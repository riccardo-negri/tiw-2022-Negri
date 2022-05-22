import mysql.connector
import random
import time

USERS_NUMBER = 10
MAX_ACCOUNTS_PER_USER = 10
MAX_MONEY = 1000000
MAX_TRANSACTIONS_PER_ACCOUNT = 10
ACCOUNT_NUMBER_LENGTH = 12
NAMES = ["Francesco", "Sofia", "Alessandro", "Giulia", "Andrea", 
        "Aurora", "Lorenzo", "Emma", "Mattia", "Giorgia"]
SURNAMES = ["Rossi", "Russo", "Ferrari", "Esposito", "Bianchi",
            "Colombo", "Ricci", "Gallo", "Conti", "Giordano"]
DEFAULT_PASSWORD = "pass"

def str_time_prop(start, end, time_format, prop):
    stime = time.mktime(time.strptime(start, time_format))
    etime = time.mktime(time.strptime(end, time_format))
    ptime = stime + prop * (etime - stime)
    return time.strftime(time_format, time.localtime(ptime))

mydb = mysql.connector.connect(
  host="localhost",
  user="admin",
  password="password",
  database="tiw_db"
)

mycursor = mydb.cursor()

accounts = []
sql_u = "INSERT INTO utente (username, password, nome, cognome) VALUES (%s, %s, %s, %s)"
sql_c = "INSERT INTO conto (codice, saldo, utente) VALUES (%s, %s, %s)"
sql_t = "INSERT INTO trasferimento (data, importo, causale, origine, destinazione) VALUES (%s, %s, %s, %s, %s)"
id_u = 0
id_c = 0
id_t = 0
for _ in range(0, USERS_NUMBER):
    name = random.choice(NAMES)
    surname = random.choice(SURNAMES)
    username = (name + surname).lower()
    password = DEFAULT_PASSWORD
    try:
        id_u += 1
        mycursor.execute(sql_u, (username, password, name, surname))
    except mysql.connector.errors.IntegrityError:
        print("HERE")
        continue
    mydb.commit()
    for _ in range(0, random.randint(0, MAX_ACCOUNTS_PER_USER)):
        account_number = '{0:05}'.format(random.randint(1, 100000))
        money = random.randint(0, MAX_MONEY)
        try:
            id_c += 1
            mycursor.execute(sql_c, (account_number, money, id_u))
            accounts.append(account_number)
        except mysql.connector.errors.IntegrityError:
            print("HERE")
            continue
        mydb.commit()

for i in range(1, id_c+1):
    for _ in range(0, random.randint(0, MAX_TRANSACTIONS_PER_ACCOUNT)):
        date = str_time_prop("2000-1-1 10:00:00", "2022-6-20 10:00:00", '%Y-%m-%d %H:%M:%S', random.random())
        amount = random.randint(1, money)
        while True:
            y = random.randint(1, id_c + 1)
            if i != y: break
        mycursor.execute(sql_t, (date, amount, "causale non specificata", i, y))
        mydb.commit()

print(mycursor.rowcount, "was insserted.")
