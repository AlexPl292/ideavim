package com.maddyhome.idea.vim.ex.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.KeyHandler;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.command.CommandState;
import com.maddyhome.idea.vim.ex.CommandHandler;
import com.maddyhome.idea.vim.ex.ExCommand;
import com.maddyhome.idea.vim.ex.ExException;
import com.maddyhome.idea.vim.ex.LineRange;
import com.maddyhome.idea.vim.handler.CaretOrder;
import com.maddyhome.idea.vim.handler.ExecuteMethodNotOverriddenException;
import com.maddyhome.idea.vim.helper.EditorHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

import static com.maddyhome.idea.vim.helper.StringHelper.parseKeys;

public class NormalHandler extends CommandHandler {
    public NormalHandler() {
        super("norm", "al", RANGE_OPTIONAL | ARGUMENT_REQUIRED | WRITABLE | SAVE_VISUAL_MODE, true,
                CaretOrder.INCREASING_OFFSET);
    }

    @Override
    public boolean execute(@NotNull Editor editor, @NotNull Caret caret, @NotNull DataContext context, @NotNull ExCommand cmd) throws ExException, ExecuteMethodNotOverriddenException {
        String argument = cmd.getArgument();
        boolean useMapping = true;
        if (!argument.isEmpty() && argument.charAt(0) == '!') {
            // Disable mapping by "!" option
            useMapping = false;
            argument = argument.substring(1).trim();
        }

        // True if line range was explicitly defined by user
        boolean rangeUsed = cmd.getRanges().size() != 0;

        LineRange range = cmd.getLineRange(editor, caret, context);

        CommandState commandState = CommandState.getInstance(editor);
        if (commandState.getMode() == CommandState.Mode.VISUAL) {
            // Disable visual mode before command execution
            // Otherwise commands will be applied to selected text
            VimPlugin.getMotion().exitVisual(editor);
        }

        for (int line = range.getStartLine(); line <= range.getEndLine(); line++) {
            if (rangeUsed) {
                // Move caret to the first position on line
                if (editor.getDocument().getLineCount() < line) {
                    break;
                }
                int startOffset = EditorHelper.getLineStartOffset(editor, line);
                editor.getCaretModel().moveToOffset(startOffset);
            }

            // Perform operations
            List<KeyStroke> keys = parseKeys(argument);
            KeyHandler keyHandler = KeyHandler.getInstance();
            keyHandler.reset(editor);
            for (KeyStroke key : keys) {
                keyHandler.handleKey(editor, key, context, useMapping);
            }

            // Exit if state leaves as insert or cmd_line
            CommandState.Mode mode = commandState.getMode();
            if (mode == CommandState.Mode.EX_ENTRY) {
                VimPlugin.getProcess().cancelExEntry(editor, context);
            }
            if (mode == CommandState.Mode.INSERT || mode == CommandState.Mode.REPLACE) {
                VimPlugin.getChange().processEscape(editor, context);
            }
        }
        return true;
    }
}
