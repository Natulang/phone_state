package it.mainella.phone_state.handler

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneStatePermissionsTest {
    @Test
    fun allowsInitialStateWhenPhoneStatePermissionIsGrantedWithoutCallLogPermission() {
        val permissions = PhoneStatePermissions(
            hasReadPhoneState = true,
            hasReadCallLog = false
        )

        assertTrue(permissions.canEmitInitialState)
    }

    @Test
    fun blocksInitialStateWhenPhoneStatePermissionIsMissing() {
        val permissions = PhoneStatePermissions(
            hasReadPhoneState = false,
            hasReadCallLog = true
        )

        assertFalse(permissions.canEmitInitialState)
    }
}
