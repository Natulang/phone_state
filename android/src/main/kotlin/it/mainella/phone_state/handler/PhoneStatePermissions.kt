package it.mainella.phone_state.handler

internal data class PhoneStatePermissions(
    val hasReadPhoneState: Boolean,
    val hasReadCallLog: Boolean
) {
    val canEmitInitialState: Boolean
        get() = hasReadPhoneState
}
