import sys

import Ice

import Bank
from Bank import *

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Program needs port number as parameter\n", file=sys.stderr)
        exit(1)
    else:
        with Ice.initialize() as communicator:
            try:
                base = communicator.stringToProxy(
                    "factory/factory:tcp -h localhost -p {}:udp -h localhost -p {}".format(sys.argv[1], sys.argv[1]))
                account_factory = Bank.AccountFactoryPrx.checkedCast(base)
            except Ice.EndpointParseException:
                print("Incorrect port", file=sys.stderr)
                exit(1)
            if not account_factory:
                print("Invalid proxy", file=sys.stderr)
                exit(1)
            while True:
                cmd = input("Please input 'login' for logging in, 'create' for account creation or 'x' to exit\n")
                if cmd == 'x':
                    break
                if cmd == 'login':
                    guid = input("Please enter GUID or 'x' to exit\n")
                    if guid == 'x':
                        continue
                    try:
                        account_proxy = account_factory.getAccount(guid)
                    except NoSuchAccountError as e:
                        print("No such account exists")
                        continue
                    except (Ice.ConnectionRefusedException, Ice.ObjectNotExistException):
                        print("Cannot connect to bank. Please restart the application.", file=sys.stderr)
                        break
                    while True:
                        cmd = input("Specify operation: 'getState' or 'requestLoan' or enter 'x' to logout\n")
                        if cmd == 'x':
                            break
                        if cmd == 'getState':
                            try:
                                account = account_proxy.getState(guid)
                                print(account)
                            except (Ice.ConnectionRefusedException, Ice.ObjectNotExistException):
                                print("Cannot connect to bank. Please restart the application.", file=sys.stderr)
                                break
                            except Exception as e:
                                print(e, file=sys.stderr)
                        elif cmd == 'requestLoan':
                            currency = input("Please enter currency or 'x' to exit\n")
                            if currency == 'x':
                                continue
                            try:
                                loan = account_proxy.requestLoan(guid, currency)
                                print(loan)
                            except (Ice.ConnectionRefusedException, Ice.ObjectNotExistException):
                                print("Cannot connect to bank. Please restart the application.", file=sys.stderr)
                                break
                            except NotPermittedError as e:
                                print(e.message, file=sys.stderr)
                            except NoSuchCurrencyError:
                                print("Bank cannot provide information for given currency", file=sys.stderr)
                elif cmd == 'create':
                    name = input("Please enter name or 'x' to exit\n")
                    if name == 'x':
                        continue
                    surname = input("Please enter surname or 'x' to exit\n")
                    if surname == 'x':
                        continue
                    pesel = input("Please enter pesel or 'x' to exit\n")
                    if pesel == 'x':
                        continue
                    try:
                        pesel = int(pesel)
                    except ValueError:
                        print("Incorrect value. PESEL must be long.\n", file=sys.stderr)
                        continue
                    income = input("Please enter income or 'x' to exit\n")
                    if income == 'x':
                        continue
                    try:
                        income = float(income)
                    except ValueError:
                        print("Incorrect value. Income must be double.\n", file=sys.stderr)
                        continue
                    guid = account_factory.create(name, surname, pesel, income)
                    print("Account created with guid '{}'".format(guid))
                else:
                    print("Unknown command.\n", file=sys.stderr)
