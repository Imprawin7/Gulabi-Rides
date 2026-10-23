import { Client } from '@stomp/stompjs';

let client = null;
const subs = new Set();

/** Opens the STOMP connection once; subscriptions made before it connects are replayed on (re)connect. */
export function connect(token) {
  if (client) return;
  client = new Client({
    brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
  });
  client.onConnect = () => subs.forEach((s) => { s.handle = client.subscribe(s.topic, (m) => s.cb(JSON.parse(m.body))); });
  client.activate();
}
export function subscribe(topic, cb) {
  const s = { topic, cb, handle: null };
  subs.add(s);
  if (client?.connected) s.handle = client.subscribe(topic, (m) => cb(JSON.parse(m.body)));
  return () => { subs.delete(s); s.handle?.unsubscribe(); };
}
export const publish = (destination, body) => client?.connected && client.publish({ destination, body: JSON.stringify(body) });
export function disconnect() { client?.deactivate(); client = null; subs.clear(); }
