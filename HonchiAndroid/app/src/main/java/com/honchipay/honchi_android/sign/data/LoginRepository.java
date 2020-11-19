package com.honchipay.honchi_android.sign.data;

import com.honchipay.honchi_android.network.HonchipayConnector;

import java.util.HashMap;

import io.reactivex.Single;
import retrofit2.Response;

public class LoginRepository {
    public Single<Response<TokenResponseData>> doLogin(String email, String password) {
        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        return HonchipayConnector.getInstance().getApi().tryDoLogin(body);
    }

    public Single<Response<Void>> findUserPassword(String email, String password) {
        HashMap<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        return HonchipayConnector.getInstance().getApi().findPassword(body);
    }
}