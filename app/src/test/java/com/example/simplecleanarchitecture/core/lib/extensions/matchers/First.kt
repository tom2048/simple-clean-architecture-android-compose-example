package com.example.simplecleanarchitecture.core.lib.extensions.matchers

import org.mockito.internal.exceptions.Reporter
import org.mockito.internal.verification.api.VerificationData
import org.mockito.internal.verification.checkers.MissingInvocationChecker
import org.mockito.verification.VerificationMode

class First : VerificationMode {
    override fun verify(data: VerificationData?) {
        data?.allInvocations?.firstOrNull()?.run {
            MissingInvocationChecker.checkMissingInvocation(listOf(this), data.target)
        } ?: run {
            throw Reporter.wantedButNotInvoked(data?.target)
        }
    }

}