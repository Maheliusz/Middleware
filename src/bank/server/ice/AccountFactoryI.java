package bank.server.ice;

import Bank.*;
import Bank.grpc.*;
import Bank.grpc.Currency;
import com.zeroc.Ice.*;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.util.*;

public class AccountFactoryI implements AccountFactory {
    private Map<String, AccountData> accountDataMap;
    private Map<Long, AccountPrx> accountPrxMap;
    private final Map<Currency, Double> courseMap;

    public AccountFactoryI( Map<Currency, Double> courseMap) {
        accountDataMap = new HashMap<>();
        accountPrxMap = new HashMap<>();
        this.courseMap = courseMap;
        Set<Currency> currencySet = new HashSet<>();
        currencySet.forEach(key -> courseMap.put(key, 0.0));
    }

    @Override
    public String create(String name, String surname, long pesel, double income, Current current) {
        AccountData data = new AccountData();
        data.income = income;
        data.name = name;
        data.surname = surname;
        data.pesel = pesel;
        if (income > 10000) data.type = accountType.PREMIUM;
        else data.type = accountType.STANDARD;
        Random random = new Random();
        data.amount = 1000 + 10000 * random.nextDouble();
        do {
            data.guid = UUID.randomUUID().toString();
        } while (accountDataMap.keySet().contains(data.guid));
        accountDataMap.put(data.guid, data);
        accountPrxMap.put(data.pesel, AccountPrx.uncheckedCast(current.adapter.add(new AccountI(data, courseMap),
                new Identity(String.valueOf(data.pesel), data.type.toString()))));
        return data.guid;
    }

    @Override
    public AccountPrx getAccount(String guid, Current current) throws NoSuchAccountError {
        AccountData data = accountDataMap.get(guid);
        if (data == null) throw new NoSuchAccountError();
        return accountPrxMap.get(data.pesel);
    }
}
