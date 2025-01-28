public enum Nation {

    한국("원"),
    일본("엔"),
    미국("달러");

    private final String currency;

    Nation(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}