
#ifndef BANK_ICE
#define BANK_ICE

module Bank
{

  enum accountType { STANDARD, PREMIUM };

  struct AccountData
  {
    string name;
    string surname;
    long pesel;
    double income;
    accountType type;
    double amount;
    string guid;
  };

  exception NotPermittedError
  {
    string message;
  };

  exception NoSuchAccountError
  {
  };

  exception NoSuchCurrencyError
  {
  };

  interface Account
  {
    AccountData getState(string guid) throws NotPermittedError;
    double requestLoan(string guid, string name) throws NotPermittedError, NoSuchCurrencyError;
  };

  interface AccountFactory
  {
      string create(string name, string surname, long pesel, double income);
      Account* getAccount(string guid) throws NoSuchAccountError;
  };
};

#endif
