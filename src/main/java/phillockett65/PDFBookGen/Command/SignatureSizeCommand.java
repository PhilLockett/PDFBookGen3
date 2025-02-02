/*  Command - a Java based Command pattern implementation.
 *
 *  Copyright 2025 Philip Lockett.
 *
 *  This file is part of Command.
 *
 *  Command is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Command is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Command.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * SignatureSizeCommand is a class that captures the setShowGuide Command.
 */
package phillockett65.PDFBookGen.Command;

import phillockett65.Debug.Debug;
import phillockett65.PDFBookGen.Model;

public class SignatureSizeCommand implements Command {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private final String className = "SignatureSizeCommand";
    private final int originalValue;
    private int newValue;
 
    public SignatureSizeCommand(int oldVal, int newVal) {
        originalValue = oldVal;
        newValue = newVal;
    }

    private void worker(int value) {
        Model model = Model.getInstance();

        model.setSigSize(value);
        model.syncSigSize();
        model.syncUI();
    }

    @Override
    public void execute() {
        Model model = Model.getInstance();

        model.syncSigSize();
        model.syncUI();
    }

    @Override
    public void undo() {
        Debug.trace(DD, "undo " + className + " -> " + originalValue);
        worker(originalValue);
    }

    @Override
    public void redo() {
        Debug.trace(DD, "redo " + className + " -> " + newValue);
        worker(newValue);
    }

    @Override
    public boolean update(Command newCommand) {
        final String name = newCommand.getClass().getSimpleName();
        if (name.compareTo(className) != 0) {
            return false;
        }

        SignatureSizeCommand command = (SignatureSizeCommand)newCommand;
        newValue = command.newValue;

        return true;
    }

    @Override
    public boolean isChanging() {
        return newValue != originalValue;
    }

    @Override
    public boolean isReverting(Command newCommand) {
        final String name = newCommand.getClass().getSimpleName();
        if (name.compareTo(className) != 0) {
            return false;
        }

        SignatureSizeCommand command = (SignatureSizeCommand)newCommand;
        if (newValue != command.originalValue) {
            return false;
        }
        if (originalValue != command.newValue) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "[" + className + "| " + originalValue + " -> " + newValue + "]";
    }
}

