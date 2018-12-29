public enum PeerState {
    RECEIVER(0),
    SENDER(1),
    IDLE(2);

    int state;

    PeerState(int state) {
        this.state = state;
    }
}
