#!/usr/bin/env python3
import logging, random, string, json, requests
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import Application, CommandHandler, CallbackQueryHandler, ContextTypes

TOKEN = "8988179447:AAEwRu3mmjy0MGeRHb4HPQQGzov5q4u8dEE"
ADMIN_CHAT_ID = 7683760111
API_URL = "http://localhost:3000/api"

PAYMENT_METHODS = {"DANA": "6285647814135", "GoPay": "6285746399596", "OVO": "6285746399596"}
orders = {}
logging.basicConfig(level=logging.INFO)

def generate_password():
    chars = string.ascii_letters + string.digits
    return ''.join(random.choices(chars, k=8))

def generate_key_from_api(duration):
    try:
        resp = requests.post(f"{API_URL}/generate-key", json={"duration": duration})
        if resp.status_code == 200:
            return resp.json()["key"]
        return None
    except:
        return None

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    try:
        await update.message.reply_photo(photo="https://files.catbox.moe/pj6pg8.jpg", caption="🔥 *NEXA BUG ULTIMATE*")
    except:
        pass
    keyboard = [
        [InlineKeyboardButton("🛒 Beli Key 1 Hari", callback_data="buy_day")],
        [InlineKeyboardButton("🛒 Beli Key 1 Bulan", callback_data="buy_month")],
        [InlineKeyboardButton("🛒 Beli Key Permanen", callback_data="buy_permanent")],
        [InlineKeyboardButton("🎁 Dapatkan Key Gratis", callback_data="free_key")],
        [InlineKeyboardButton("🔑 Aktivasi Key", callback_data="activate")],
        [InlineKeyboardButton("💳 Metode Pembayaran", callback_data="payment_info")],
        [InlineKeyboardButton("📱 Hubungi Admin", callback_data="contact_admin")],
        [InlineKeyboardButton("ℹ️ Info Bot", callback_data="info")],
    ]
    reply_markup = InlineKeyboardMarkup(keyboard)
    await update.message.reply_text(
        "🤖 *NEXA BUG ULTIMATE*\n\n"
        "🔥 Bug WhatsApp Premium\n"
        "📱 Pairing Code (tanpa QR)\n"
        "💀 8 Jenis Bug Mematikan\n\n"
        "Pilih menu di bawah:",
        reply_markup=reply_markup, parse_mode="Markdown"
    )

async def button_handler(update: Update, context: ContextTypes.DEFAULT_TYPE):
    query = update.callback_query
    await query.answer()
    data = query.data
    user_id = update.effective_user.id

    if data.startswith("buy_"):
        duration = data.replace("buy_", "")
        durasi_text = {"day": "1 Hari", "month": "1 Bulan", "permanent": "Permanen"}
        prices = {"day": "Rp 5.000", "month": "Rp 25.000", "permanent": "Rp 100.000"}
        key = generate_key_from_api(duration)
        if not key:
            await query.edit_message_text("❌ Gagal generate key, coba lagi.")
            return
        password = generate_password()
        orders[user_id] = {"duration": duration, "key": key, "password": password, "paid": False}
        pay_msg = "💳 *Cara Bayar:*\n"
        for method, number in PAYMENT_METHODS.items():
            pay_msg += f"   • {method}: `{number}`\n"
        pay_msg += f"\n💰 *Harga:* {prices.get(duration, 'Rp 5.000')}"
        pay_msg += "\n\n📌 Transfer ke salah satu nomor di atas."
        pay_msg += "\n📱 Setelah transfer, klik *'Konfirmasi Bayar'*."
        keyboard = [
            [InlineKeyboardButton("✅ Konfirmasi Bayar", callback_data="confirm_payment")],
            [InlineKeyboardButton("❌ Batal", callback_data="cancel_order")],
        ]
        reply_markup = InlineKeyboardMarkup(keyboard)
        await query.edit_message_text(
            f"🛒 *Order Key {durasi_text.get(duration, duration)}*\n\n"
            f"📋 *Key:* `{key}` (belum aktif)\n"
            f"🔑 *Password:* `{password}` (belum aktif)\n\n"
            f"{pay_msg}",
            reply_markup=reply_markup, parse_mode="Markdown"
        )

    elif data == "confirm_payment":
        if user_id not in orders or orders[user_id]["paid"]:
            await query.edit_message_text("❌ Tidak ada order aktif atau sudah dibayar.")
            return
        order = orders[user_id]
        order["paid"] = True
        key = order["key"]
        password = order["password"]
        duration = order["duration"]
        durasi_text = {"day": "1 Hari", "month": "1 Bulan", "permanent": "Permanen"}
        await query.edit_message_text(
            f"✅ *Pembayaran berhasil!*\n\n"
            f"📋 *Key:* `{key}`\n"
            f"🔑 *Password:* `{password}`\n\n"
            f"⏳ Berlaku: {durasi_text.get(duration, duration)}\n"
            f"🔗 Masukkan di APK untuk aktivasi.",
            parse_mode="Markdown"
        )
        del orders[user_id]
        try:
            await context.bot.send_message(chat_id=ADMIN_CHAT_ID, text=f"✅ User {user_id} konfirmasi bayar.\nKey: {key}\nPassword: {password}")
        except:
            pass

    elif data == "cancel_order":
        if user_id in orders:
            del orders[user_id]
            await query.edit_message_text("❌ Order dibatalkan.")
        else:
            await query.edit_message_text("❌ Tidak ada order aktif.")

    elif data == "free_key":
        key = generate_key_from_api("day")
        if not key:
            await query.edit_message_text("❌ Gagal generate key.")
            return
        password = generate_password()
        await query.edit_message_text(
            f"🎁 *Key Gratis (Demo)*\n\n"
            f"📋 Key: `{key}`\n🔑 Password: `{password}`\n\n⏳ Berlaku 1 hari",
            parse_mode="Markdown"
        )

    elif data == "activate":
        await query.edit_message_text(
            "🔑 *Cara Aktivasi Key:*\n\n"
            "1. Buka APK Nexa Bug\n"
            "2. Masukkan key & password\n"
            "3. Klik 'Aktivasi'\n\n✅ Key valid = akses terbuka.",
            parse_mode="Markdown"
        )

    elif data == "payment_info":
        msg = "💳 *Metode Pembayaran:*\n\n"
        for method, number in PAYMENT_METHODS.items():
            msg += f"• {method}: `{number}`\n"
        msg += "\n📌 Transfer sesuai harga, klik 'Konfirmasi Bayar'."
        await query.edit_message_text(msg, parse_mode="Markdown")

    elif data == "contact_admin":
        await query.edit_message_text(
            "📱 *Hubungi Admin:*\n\n💬 @kiyy_official1\n📧 kiyy.official@gmail.com",
            parse_mode="Markdown"
        )

    elif data == "info":
        await query.edit_message_text(
            "ℹ️ *NEXA BUG ULTIMATE*\n\n"
            "🔹 8 jenis bug WhatsApp\n"
            "🔹 Forclose, Delay, Spam, Onehit\n"
            "🔹 Pairing Code (no QR)\n"
            "🔹 Developer: KiYY OFFICIAL\n"
            "💬 Admin: @kiyy_official1\n"
            "💳 Payment: DANA / GoPay / OVO",
            parse_mode="Markdown"
        )

def main():
    app = Application.builder().token(TOKEN).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(CommandHandler("help", start))
    app.add_handler(CallbackQueryHandler(button_handler))
    print("🤖 Bot running...")
    app.run_polling()

if __name__ == "__main__":
    main()
