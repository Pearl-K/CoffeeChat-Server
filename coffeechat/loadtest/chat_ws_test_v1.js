import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// 1) 메트릭 선언
export let sentCounter    = new Counter('sent_messages');
export let recvCounter    = new Counter('received_messages');
export let connectTrend   = new Trend('ws_connect_time');

// 2) 옵션: 동시 VU 수와 테스트 지속 시간 설정
export let options = {
    vus: 100,          // 100명의 가상 사용자
    duration: '1m',    // 1분간 실행
};

const WS_URL     = 'ws://localhost:8080/ws';
const SUB_PREFIX = '/topic/chatroom/';
const SEND_DEST  = '/app/chat/sendMessage';
const ROOM_MIN   = 1;
const ROOM_MAX   = 5;
const SENDER_MAX = 1000;

export default function () {
    // VU별 고유값
    const roomId    = Math.floor(Math.random() * (ROOM_MAX - ROOM_MIN + 1)) + ROOM_MIN;
    const senderId  = (__VU - 1) % SENDER_MAX + 1;
    const senderName= `user${senderId}`;
    const subscribeReceipt = `rcpt-sub-${__VU}-${roomId}`;
    const disconnectReceipt= `rcpt-disc-${__VU}-${roomId}`;

    // 3) 최초 이터레이션에서만 연결
    if (__ITER === 0) {
        console.log(`VU${__VU} ▶ Attempting WS connect to ${WS_URL}`);
        const res = ws.connect(WS_URL, {
            headers: { 'Sec-WebSocket-Protocol': 'v10.stomp, v11.stomp' }
        }, socket => {
            // 3-1) 열리면 STOMP CONNECT
            socket.on('open', () => {
                console.log(`VU${__VU} ▶ WS open, sending STOMP CONNECT`);
                socket.send(
                    'CONNECT\n' +
                    'accept-version:1.2\n' +
                    'host:localhost\n' +
                    'heart-beat:0,0\n\n' +
                    '\0'
                );
            });

            // 3-2) 서버 프레임 처리
            socket.on('message', (data, isBinary) => {
                const text = isBinary ? data : data.toString();

                // 로그 raw frame
                console.log(`VU${__VU} ◀ Frame received:\n${text}`);

                // CONNECTED → SUBSCRIBE
                if (text.startsWith('CONNECTED')) {
                    console.log(`VU${__VU} ▶ Received CONNECTED, sending SUBSCRIBE to ${SUB_PREFIX}${roomId}`);
                    socket.send(
                        'SUBSCRIBE\n' +
                        `id:sub-${__VU}-${roomId}\n` +
                        `destination:${SUB_PREFIX}${roomId}\n` +
                        `receipt:${subscribeReceipt}\n\n` +
                        '\0'
                    );
                }
                // 구독 완료 확인 → 메시지 발행 루프 시작
                else if (text.includes(`RECEIPT\nreceipt-id:${subscribeReceipt}`)) {
                    console.log(`VU${__VU} ▶ Subscription confirmed (receipt:${subscribeReceipt}), starting send loop`);
                    const endSendTime = Date.now() + 60 * 1000;
                    while (Date.now() < endSendTime) {
                        console.log(`VU${__VU} ▶ Sending batch of 5 messages to room ${roomId}`);
                        for (let i = 0; i < 5; i++) {
                            const payload = JSON.stringify({
                                chatroomId: roomId,
                                senderId,
                                senderName,
                                message: `msg#${i+1} from ${senderName}`,
                                timestamp: new Date().toISOString(),
                            });
                            socket.send(
                                'SEND\n' +
                                `destination:${SEND_DEST}\n` +
                                'content-type:application/json\n\n' +
                                payload + '\0'
                            );
                            sentCounter.add(1);
                        }
                        sleep(10);  // 10초 대기
                    }
                    console.log(`VU${__VU} ▶ Send loop completed, sending DISCONNECT (receipt:${disconnectReceipt})`);
                    socket.send(
                        'DISCONNECT\n' +
                        `receipt:${disconnectReceipt}\n\n` +
                        '\0'
                    );
                }
                // 서버가 DISCONNECT 처리 완료했음을 알리면 close()
                else if (text.includes(`RECEIPT\nreceipt-id:${disconnectReceipt}`)) {
                    console.log(`VU${__VU} ▶ Disconnect confirmed (receipt:${disconnectReceipt}), closing socket`);
                    socket.close();
                }
                // MESSAGE 수신 시 카운트
                else if (text.startsWith('MESSAGE')) {
                    console.log(`VU${__VU} ▶ MESSAGE received`);
                    recvCounter.add(1);
                }
            });

            socket.on('error', e => console.error(`VU${__VU} socket error:`, e));
            socket.on('close', () => {
                console.log(`VU${__VU} ▶ socket closed`);
            });
        });

        // 연결 시간 메트릭
        connectTrend.add(res.timings.connect);
        check(res, { 'ws connected': r => r && r.status === 101 });
    }

    // 4) VU를 살아있게 유지
    sleep(60);  // 최대 1분 유지
}