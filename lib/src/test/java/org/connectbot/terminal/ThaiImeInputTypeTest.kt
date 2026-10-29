/*
 * ConnectBot Terminal
 * Copyright 2025 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connectbot.terminal

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputMethodManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThaiImeInputTypeTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val imm get() = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private fun makeHandler(ctrlActive: Boolean = false): KeyboardHandler {
        val emulator = TerminalEmulatorFactory.create(initialRows = 24, initialCols = 80)
        val modifierManager = object : ModifierManager {
            override fun isCtrlActive() = ctrlActive
            override fun isAltActive() = false
            override fun isShiftActive() = false
            override fun clearTransients() {}
        }
        return KeyboardHandler(emulator, modifierManager = modifierManager)
    }

    private fun makeView(handler: KeyboardHandler): ImeInputView = ImeInputView(
        context = context,
        keyboardHandler = handler,
        inputMethodManager = imm,
        onUpdateSelection = { _: View, _, _, _, _ -> },
        onRestartInput = { },
    )

    @Test
    fun normalTerminalModeUsesTextClassInputType() {
        val view = makeView(makeHandler())
        val attrs = EditorInfo()

        view.onCreateInputConnection(attrs)

        assertEquals(InputType.TYPE_CLASS_TEXT, attrs.inputType and InputType.TYPE_MASK_CLASS)
        assertTrue(attrs.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0)
        assertNotEquals(InputType.TYPE_NULL, attrs.inputType and InputType.TYPE_MASK_CLASS)
        assertTrue(attrs.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD != 0)
    }

    @Test
    fun composeModeWithForceAsciiKeepsTextClassInputType() {
        val view = makeView(makeHandler(ctrlActive = true))
        view.isComposeModeActive = true
        view.syncShortcutInputMode(ImeShortcutInputMode.FORCE_ASCII)
        val attrs = EditorInfo()

        val ic = view.onCreateInputConnection(attrs)

        assertEquals(InputType.TYPE_CLASS_TEXT, attrs.inputType and InputType.TYPE_MASK_CLASS)
        assertTrue(attrs.imeOptions and EditorInfo.IME_FLAG_FORCE_ASCII != 0)
        assertNotNull(ic.getExtractedText(ExtractedTextRequest(), 0))
    }

    @Test
    fun composeModeWithTypeNullShortcutKeepsRawKeyMode() {
        val view = makeView(makeHandler(ctrlActive = true))
        view.isComposeModeActive = true
        view.syncShortcutInputMode(ImeShortcutInputMode.TYPE_NULL)
        val attrs = EditorInfo()

        view.onCreateInputConnection(attrs)

        assertEquals(InputType.TYPE_NULL, attrs.inputType and InputType.TYPE_MASK_CLASS)
    }
}
