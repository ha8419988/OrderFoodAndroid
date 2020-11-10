package com.example.orderfoodandroid.model;

/*Token will 2 property
-token :String
-isSeverToken:boolean
isSeverToken will flag tell us this token is from Client or Sever
and Each Token will Storage at Firebase by Key(Key is UserPhone)
so 1 User  just have 1 token
*/
public class Token {
    private String token;
    private Boolean isServerToken;

    public Token() {
    }

    public Token(String token, Boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getServerToken() {
        return isServerToken;
    }

    public void setServerToken(Boolean serverToken) {
        isServerToken = serverToken;
    }
}
