package io.kickflip.sdk;

public class KickflipArguments {

    private Communicator communicator;

    public KickflipArguments(Communicator communicator) {
        this.communicator = communicator;
    }

    public Communicator getCommunicator() {
        return communicator;
    }
}
