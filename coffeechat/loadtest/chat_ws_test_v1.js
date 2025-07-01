import ws from 'k6/ws';
import { check, sleep } from 'k6';

export let options = {
    // 1) Ramp-up → Sustain → Ramp-down
    stages: [
        { duration: '30s', target: 100 },  // 0→100 VUs를 30초간 점진적으로 증가
        { duration: '1m',  target: 100 },  // 100 VU 유지하며 3분간 테스트
        { duration: '30s', target:   0 },  // 100→0 VU를 30초간 점진적으로 감소
    ],
    // 2) (대안) stages 대신에 고정 VU+duration만 사용할 때
    // vus: 100,
    // duration: '5m',
};

const WS_URL    = 'ws://localhost:8080/coffee-chat-ws';
const SUB_PREFIX = '/topic/chatroom/';
const SEND_DEST  = '/app/chat/sendMessage';

// 채팅방(ID)의 최소·최대 범위
const ROOM_MIN = 1;
const ROOM_MAX = 10;
// senderId의 최대값 (예: 최대 1,000명)
const SENDER_MAX = 1000;

export default function () {
    // 매 VU가 돌 때마다 랜덤 채팅방·보낸이 ID 결정
    const roomId   = Math.floor(Math.random() * (ROOM_MAX - ROOM_MIN + 1)) + ROOM_MIN;
    const senderId = (__VU - 1) % SENDER_MAX + 1;    // 또는 Math.floor(Math.random()*SENDER_MAX)+1
    const senderName = `user${senderId}`;

    const payload = JSON.stringify({
        chatroomId: roomId,
        senderId:   senderId,
        senderName: senderName,
        message:    `Hello from ${senderName} in room ${roomId}`,
    });

    const res = ws.connect(WS_URL, {
            headers: {
                'Sec-WebSocket-Protocol': 'v10.stomp, v11.stomp'
            }
        }, socket => {

        // 1) STOMP CONNECT
        socket.send(
            'CONNECT\n' +
            'accept-version:1.2\n' +
            'host:localhost\n\n' +
            '\0'
        );

        // 2) SUBSCRIBE to this VU's room
        socket.send(
            'SUBSCRIBE\n' +
            `id:sub-${__VU}\n` +
            `destination:${SUB_PREFIX}${roomId}\n\n` +
            '\0'
        );

        // 3) 메시지 송신 루프
        //    – VU당 10회 메시지를 평균 5초 간격으로
        for (let i = 0; i < 10; i++) {
            socket.send(
                'SEND\n' +
                `destination:${SEND_DEST}\n` +
                'content-type:application/json\n\n' +
                payload + '\0'
            );
            sleep(5);  // 메시지 간 평균 간격(초)
        }

        socket.close();
    });
    check(res, { 'connected (101)': r => r && r.status === 101 });
}
