import mysql.connector
import random
import time

USERS_NUMBER = 10
MAX_ACCOUNTS_PER_USER = 10
MAX_MONEY = 1000000
MAX_TRANSACTIONS_PER_ACCOUNT = 10
MAX_CONTACTS_PER_USER = 10
ACCOUNT_NUMBER_LENGTH = 12
NAMES = ["Francesco", "Sofia", "Alessandro", "Giulia", "Andrea", 
        "Aurora", "Lorenzo", "Emma", "Mattia", "Giorgia"]
SURNAMES = ["Rossi", "Russo", "Ferrari", "Esposito", "Bianchi",
            "Colombo", "Ricci", "Gallo", "Conti", "Giordano"]
DEFAULT_PASSWORD = "pass"

def str_time_prop(start, end, time_format):
    stime = time.mktime(time.strptime(start, time_format))
    etime = time.mktime(time.strptime(end, time_format))
    ptime = stime + random.random() * (etime - stime)
    return time.strftime(time_format, time.localtime(ptime))

mydb = mysql.connector.connect(
  host="localhost",
  user="admin",
  password="password",
  database="tiw_db"
)

mycursor = mydb.cursor()


accounts = {}
accounts_money = {}
sql_u = "INSERT INTO user(username, email, password, name, surname) VALUES (%s, %s, %s, %s, %s)"
sql_a = "INSERT INTO account (code, balance, user) VALUES (%s, %s, %s)"
sql_t = "INSERT INTO transaction (timestamp, amount, reason, origin, destination) VALUES (%s, %s, %s, %s, %s)"
sql_c = "INSERT INTO contact (owner, element) VALUES (%s, %s)"
id_u = 0
id_c = 0
id_t = 0
count_u = 0
count_a = 0
count_t = 0
count_c = 0
for _ in range(0, USERS_NUMBER):
    name = random.choice(NAMES)
    surname = random.choice(SURNAMES)
    username = (name + surname).lower()
    email = (name + "." + surname + "@mail.com").lower()
    password = DEFAULT_PASSWORD
    try:
        id_u += 1
        mycursor.execute(sql_u, (username, email, password, name, surname))
        count_u += 1
    except mysql.connector.errors.IntegrityError:
        #print("HERE")
        continue
    mydb.commit()
    for _ in range(0, random.randint(0, MAX_ACCOUNTS_PER_USER)):
        account_number = '{0:05}'.format(random.randint(1, 100000))
        money = random.randint(0, MAX_MONEY)
        try:
            id_c += 1
            mycursor.execute(sql_a, (account_number, money, id_u))
            accounts[id_u] = (id_c)
            accounts_money[id_c] = money
            count_a += 1
        except mysql.connector.errors.IntegrityError:
            #print("HERE")
            continue
        mydb.commit()

for i in accounts.keys():
    for _ in range(0, random.randint(0, MAX_TRANSACTIONS_PER_ACCOUNT)):
        while True:
            y = random.choice(list(accounts.values()))
            if accounts[i] != y: break
        if accounts_money[accounts[i]] > 1:
            amount = random.randint(1, accounts_money[accounts[i]])
        accounts_money[accounts[i]] -= amount
        timestamp = str_time_prop("2000-1-1 00:00:00", "2022-6-20 00:00:00", "%Y-%m-%d %H:%M:%S")
        mycursor.execute(sql_t, (timestamp, amount, "causale non specificata. {} -> {}".format(accounts[i], y), accounts[i], y))
        mydb.commit()
        count_t += 1

        if random.randint(1,2) == 1:
            mycursor.execute(sql_c, (i, y))
            count_c += 1
        mydb.commit()

print(count_u, "users were inserted.")
print(count_a, "bank accounts were inserted.")
print(count_t, "transactions were inserted.")
print(count_c, "contacts were inserted.")
