import csv
import os
from pathlib import Path
from flask import Flask, request, jsonify
from flask_cors import CORS
import re

app = Flask(__name__)
CORS(app)

BASE_DIR = Path(__file__).resolve().parent
USERS_FILE = BASE_DIR / 'users.csv'
STRUCTURE_FILE = BASE_DIR / 'home_structure.csv'
FIELDNAMES = ['username', 'house_id', 'house_name', 'country', 'state', 'city', 'floor_id', 'floor_name', 'room_id', 'room_name', 'device_id', 'device_name', 'device_type', 'status']

def is_valid_username(username):
    return bool(re.match("^[a-zA-Z0-9]+$", username))

def read_csv(file_path):
    if not os.path.exists(file_path): return []
    with open(file_path, mode='r', newline='', encoding='utf-8') as f:
        return list(csv.DictReader(f))

def write_csv(file_path, fieldnames, data):
    with open(file_path, mode='w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)

@app.route('/register', methods=['POST'])
def register():
    data = request.json
    username = data.get('username', '').lower()
    if not is_valid_username(username):
        return jsonify({"success": False, "message": "Invalid username. Only letters and numbers allowed."}), 400
    users = read_csv(USERS_FILE)
    if any(u['username'].lower() == username for u in users):
        return jsonify({"success": False, "message": "Username already exists"}), 400
    users.append({"username": username, "password": data.get('password')})
    write_csv(USERS_FILE, ['username', 'password'], users)
    return jsonify({"success": True})

@app.route('/login', methods=['POST'])
def login():
    data = request.json
    username = data.get('username', '').lower()
    users = read_csv(USERS_FILE)
    user = next((u for u in users if u['username'].lower() == username and u['password'] == data.get('password')), None)
    return jsonify({"success": True, "username": username}) if user else (jsonify({"success": False, "message": "Invalid credentials"}), 401)

def build_user_config(user_rows):
    houses_dict = {}
    for row in user_rows:
        h_id = row['house_id']
        if h_id not in houses_dict:
            houses_dict[h_id] = {
                "id": h_id, "name": row['house_name'], "country": row.get('country', ''),
                "state": row.get('state', ''), "city": row.get('city', ''), "floors_dict": {}
            }
        f_id = row['floor_id']
        if not f_id: continue
        if f_id not in houses_dict[h_id]["floors_dict"]:
            houses_dict[h_id]["floors_dict"][f_id] = {"id": f_id, "name": row['floor_name'], "rooms_dict": {}}
        r_id = row['room_id']
        if not r_id: continue
        if r_id not in houses_dict[h_id]["floors_dict"][f_id]["rooms_dict"]:
            icon = "🏠"
            if "bedroom" in r_id: icon = "🛏️"
            elif "living" in r_id: icon = "🛋️"
            elif "kitchen" in r_id: icon = "🍳"
            elif "bath" in r_id: icon = "🚿"
            houses_dict[h_id]["floors_dict"][f_id]["rooms_dict"][r_id] = {"id": r_id, "name": row['room_name'], "icon": icon, "devices": []}
        d_id = row['device_id']
        if not d_id: continue
        d_icon = "🔌"
        if row['device_type'] == "light": d_icon = "💡"
        elif row['device_type'] == "fan": d_icon = "🌬️"
        elif row['device_type'] == "ac": d_icon = "❄️"
        elif row['device_type'] == "tv": d_icon = "📺"
        houses_dict[h_id]["floors_dict"][f_id]["rooms_dict"][r_id]["devices"].append({
            "id": d_id, "name": row['device_name'], "type": row['device_type'], "icon": d_icon, "status": row['status']
        })
    result = []
    for h in houses_dict.values():
        h["floors"] = []
        for f in h["floors_dict"].values():
            f["rooms"] = list(f["rooms_dict"].values())
            del f["rooms_dict"]
            h["floors"].append(f)
        del h["floors_dict"]
        result.append(h)
    return result

@app.route('/config/<username>', methods=['GET'])
def get_config(username):
    structure = read_csv(STRUCTURE_FILE)
    user_rows = [row for row in structure if row['username'].lower() == username.lower()]
    return jsonify(build_user_config(user_rows))

@app.route('/all_configs', methods=['GET'])
def get_all_configs():
    users = read_csv(USERS_FILE)
    structure = read_csv(STRUCTURE_FILE)
    all_data = []
    for user_row in users:
        username = user_row['username'].lower()
        user_rows = [row for row in structure if row['username'].lower() == username]
        all_data.append({
            "username": username,
            "houses": build_user_config(user_rows)
        })
    return jsonify(all_data)

@app.route('/add_element', methods=['POST'])
def add_element():
    data = request.json
    new_row = {field: data.get(field, "") for field in FIELDNAMES}
    new_row['username'] = new_row['username'].lower()
    if new_row['device_id']: new_row['status'] = data.get('status', 'OFF')
    else: new_row['status'] = ""
    structure = read_csv(STRUCTURE_FILE)
    structure.append(new_row)
    write_csv(STRUCTURE_FILE, FIELDNAMES, structure)
    return jsonify({"success": True})

@app.route('/delete_element', methods=['POST'])
def delete_element():
    data = request.json
    username = data.get('username', '').lower()
    h_id, f_id, r_id, d_id = data.get('house_id'), data.get('floor_id'), data.get('room_id'), data.get('device_id')
    structure = read_csv(STRUCTURE_FILE)
    new_structure = [row for row in structure if not (row['username'].lower() == username and (not h_id or row['house_id'] == h_id) and (not f_id or row['floor_id'] == f_id) and (not r_id or row['room_id'] == r_id) and (not d_id or row['device_id'] == d_id))]
    write_csv(STRUCTURE_FILE, FIELDNAMES, new_structure)
    return jsonify({"success": True})

@app.route('/update_status', methods=['POST'])
def update_status():
    data = request.json
    username = data.get('username', '').lower()
    structure = read_csv(STRUCTURE_FILE)
    for row in structure:
        if row['username'].lower() == username and row['house_id'] == data['house_id'] and row['floor_id'] == data['floor_id'] and row['room_id'] == data['room_id'] and row['device_id'] == data['device_id']:
            row['status'] = data['status']
            break
    write_csv(STRUCTURE_FILE, FIELDNAMES, structure)
    return jsonify({"success": True})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
