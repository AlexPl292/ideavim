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

package com.maddyhome.idea.vim.action.window

import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.VimCommandAction
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.handler.EditorActionHandlerBase
import java.util.*
import javax.swing.KeyStroke

/**
 * @author Alex Plate
 */
private object LookupDownActionHandler : EditorActionHandlerBase() {
  override fun execute(editor: Editor, context: DataContext, cmd: Command): Boolean {
    val activeLookup = LookupManager.getActiveLookup(editor)
    if (activeLookup != null) {
      IdeEventQueue.getInstance().flushDelayedKeyEvents()
      EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
        .execute(editor, editor.caretModel.primaryCaret, context)
    } else {
      val keyStroke = LookupDownAction().keyStrokesSet.first().first()
      val actions = VimPlugin.getKey().getKeymapConflicts(keyStroke)
      for (action in actions) {
        if (KeyHandler.executeAction(action, context)) break
      }
    }
    return true
  }
}

class LookupDownAction : VimCommandAction(LookupDownActionHandler) {
  override fun getMappingModes(): MutableSet<MappingMode> = MappingMode.I

  override fun getKeyStrokesSet(): MutableSet<MutableList<KeyStroke>> = parseKeysSet("<C-N>")

  override fun getType(): Command.Type = Command.Type.OTHER_READONLY

  override fun getFlags(): EnumSet<CommandFlags> = EnumSet.of(CommandFlags.FLAG_TYPEAHEAD_SELF_MANAGE)
}