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
accounts_money = {}
sql_u = "INSERT INTO utente (username, password, nome, cognome) VALUES (%s, %s, %s, %s)"
sql_c = "INSERT INTO conto (codice, saldo, utente) VALUES (%s, %s, %s)"
sql_t = "INSERT INTO trasferimento (importo, causale, origine, destinazione) VALUES (%s, %s, %s, %s)"
id_u = 0
id_c = 0
id_t = 0
count_u = 0
count_c = 0
count_t = 0
for _ in range(0, USERS_NUMBER):
    name = random.choice(NAMES)
    surname = random.choice(SURNAMES)
    username = (name + surname).lower()
    password = DEFAULT_PASSWORD
    try:
        id_u += 1
        mycursor.execute(sql_u, (username, password, name, surname))
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
            mycursor.execute(sql_c, (account_number, money, id_u))
            accounts.append(id_c)
            accounts_money[id_c] = money
            count_c += 1
        except mysql.connector.errors.IntegrityError:
            #print("HERE")
            continue
        mydb.commit()

for i in accounts:
    for _ in range(0, random.randint(0, MAX_TRANSACTIONS_PER_ACCOUNT)):
        while True:
            y = random.choice(accounts)
            if i != y: break
        if accounts_money[i] > 1:    
            amount = random.randint(1, accounts_money[i])
        accounts_money[i] -= amount
        mycursor.execute(sql_t, (amount, "causale non specificata. {} -> {}".format(i, y), i, y))
        mydb.commit()
        count_t += 1

print(count_u, "users were inserted.")
print(count_c, "bank accounts were inserted.")
print(count_t, "transactions were inserted.")
