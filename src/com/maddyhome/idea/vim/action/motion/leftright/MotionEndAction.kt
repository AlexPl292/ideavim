/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2019 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.action.motion.leftright

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.VimCommandAction
import com.maddyhome.idea.vim.command.*
import com.maddyhome.idea.vim.group.MotionGroup
import com.maddyhome.idea.vim.handler.NonShiftedSpecialKeyHandler
import com.maddyhome.idea.vim.helper.vimLastColumn
import com.maddyhome.idea.vim.option.BoundStringOption
import com.maddyhome.idea.vim.option.Options
import java.util.*
import javax.swing.KeyStroke

private object MotionEndActionHandler : NonShiftedSpecialKeyHandler() {
  override fun offset(editor: Editor, caret: Caret, context: DataContext, count: Int,
                      rawCount: Int, argument: Argument?): Int {
    var allow = false
    if (CommandState.inInsertMode(editor)) {
      allow = true
    } else if (CommandState.inVisualMode(editor) || CommandState.inSelectMode(editor)) {
      val opt = Options.getInstance().getOption("selection") as BoundStringOption
      if (opt.value != "old") {
        allow = true
      }
    }

    return VimPlugin.getMotion().moveCaretToLineEndOffset(editor, caret, count - 1, allow)
  }

  override fun preMove(editor: Editor, caret: Caret, context: DataContext, cmd: Command) {
    caret.vimLastColumn = MotionGroup.LAST_COLUMN
  }

  override fun postMove(editor: Editor, caret: Caret, context: DataContext, cmd: Command) {
    caret.vimLastColumn = MotionGroup.LAST_COLUMN
  }
}

class MotionEndAction : VimCommandAction(MotionEndActionHandler) {
  override fun getMappingModes(): MutableSet<MappingMode> = MappingMode.NVS

  override fun getKeyStrokesSet(): MutableSet<MutableList<KeyStroke>> = parseKeysSet("<End>")

  override fun getType(): Command.Type = Command.Type.MOTION

  override fun getFlags(): EnumSet<CommandFlags> = EnumSet.of(CommandFlags.FLAG_MOT_EXCLUSIVE)
}
