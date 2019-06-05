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

package com.maddyhome.idea.vim.action.copy

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.action.VimCommandAction
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.group.copy.PutData
import com.maddyhome.idea.vim.group.visual.VimSelection
import com.maddyhome.idea.vim.handler.VisualOperatorActionHandler
import java.util.*
import javax.swing.KeyStroke

private object PutVisualTextNoIndentActionHandler : VisualOperatorActionHandler.SingleExecution() {
  override fun executeForAllCarets(editor: Editor, context: DataContext, cmd: Command, caretsAndSelections: Map<Caret, VimSelection>): Boolean {
    if (caretsAndSelections.isEmpty()) return false
    val textData = VimPlugin.getRegister().lastRegister?.let { PutData.TextData(it.text, it.type) }
    VimPlugin.getRegister().resetRegister()

    val insertBeforeCaret = cmd.keys[1].keyChar == 'P'
    val selection = PutData.VisualSelection(caretsAndSelections, caretsAndSelections.values.first().type)
    val putData = PutData(textData, selection, cmd.count, insertBeforeCaret, _indent = false, caretAfterInsertedText = false)

    return VimPlugin.getPut().putText(editor, context, putData)
  }
}

/**
 * @author vlan
 */
class PutVisualTextNoIndentAction : VimCommandAction(PutVisualTextNoIndentActionHandler) {

  override fun getMappingModes(): Set<MappingMode> = MappingMode.V

  override fun getKeyStrokesSet(): Set<List<KeyStroke>> = parseKeysSet("[p", "]p", "[P", "]P")

  override fun getType(): Command.Type = Command.Type.PASTE

  override fun getFlags(): EnumSet<CommandFlags> = EnumSet.of(CommandFlags.FLAG_EXIT_VISUAL)
}
