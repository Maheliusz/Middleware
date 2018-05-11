package bank.server.ice;

import Bank.*;
import Bank.grpc.Currency;
import com.zeroc.Ice.Current;
import com.zeroc.IceInternal.Ex;

import java.util.Map;

public class AccountI implements Account {
    private AccountData accountData;
    private final Map<Currency, Double> courseMap;

    public AccountI(AccountData data, Map<Currency, Double> courseMap) {
        this.accountData = data;
        this.courseMap = courseMap;
    }

    @Override
    public AccountData getState(String guid, Current current) {
        return accountData;
    }

    @Override
    public double requestLoan(String guid, String name, Current current) throws NotPermittedError, NoSuchCurrencyError {
        if (accountData.type.equals(accountType.PREMIUM)) {
            if (!accountData.guid.equals(guid)) {
                throw createPermissionError("Incorrect GUID");
            }
            double value = 0.0;
            try {
                value = courseMap.get(Currency.valueOf(name.trim()));
            } catch (NullPointerException | IllegalArgumentException e) {
                throw new NoSuchCurrencyError();
            }
            return value;
        } else {
            throw createPermissionError("Your account does not have sufficient permissions for this operation");
        }
    }

    private NotPermittedError createPermissionError(String message) {
        NotPermittedError e = new NotPermittedError();
        e.message = message;
        return e;
    }
}
