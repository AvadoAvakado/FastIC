public enum HashDetailing {
    HASH_DETAILING_2(2),
    HASH_DETAILING_3(3),
    HASH_DETAILING_4(4),
    HASH_DETAILING_5(5),
    HASH_DETAILING_6(6);
    private byte hashDetailing;

    HashDetailing(int hashDetailing) {
        this.hashDetailing = (byte) hashDetailing;
    }

    public byte getHashDetailing() {
        return hashDetailing;
    }
}
