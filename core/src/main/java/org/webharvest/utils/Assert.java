/*
 Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
 subject line.
 */

package org.webharvest.utils;

import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 23, 2010
 * Time: 10:46:47 PM
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Assert {

    public static void isNull(Object obj) {
        isNull(obj, "Expected null reference, but was {0}", obj);
    }

    public static void isNull(Object obj, String messagePattern, Object... args) {
        isTrue(obj == null, messagePattern, args);
    }

    public static void notNull(Object obj) {
        notNull(obj, "Should not be null");
    }

    public static void notNull(Object obj, String messagePattern, Object... args) {
        isTrue(obj != null, messagePattern, args);
    }

    public static void isTrue(boolean bool, String messagePattern, Object... args) {
        if (!bool) {
            throw new AssertionError(MessageFormat.format(messagePattern, args));
        }
    }

    public static void isFalse(boolean bool, String messagePattern, Object... args) {
        if (bool) {
            throw new AssertionError(MessageFormat.format(messagePattern, args));
        }
    }

    public static IllegalStateException shouldNeverHappen(Throwable th) {
        throw new IllegalStateException("This should NEVER happen", th);
    }
}
