package de.jannik.createrailwaysignal.config;

public record BankingSettings(boolean enforced, boolean tiltRiders, boolean invertTrack,
                             boolean invertBogey, boolean invertBody) {
    public BankingSettings effective(BankingSettings personal) {
        return enforced ? this : personal;
    }
}
