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
 * GenerateCommand is a class that captures the setShowGuide Command.
 */
package phillockett65.PDFBookGen.Command;

import phillockett65.Debug.Debug;
import phillockett65.PDFBookGen.Model;

public class GenerateCommand implements Command {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private final String className = "GenerateCommand";

    public GenerateCommand() {
    }

    private void worker() {
        Model model = Model.getInstance();

        model.getController().fileSaved(model.generate());
    }

    @Override
    public void execute() {
        worker();
    }

    @Override
    public void undo() {
        Debug.trace(DD, "undo " + className);
        worker();
    }

    @Override
    public void redo() {
        Debug.trace(DD, "redo " + className);
        execute();
    }

    @Override
    public boolean update(Command newCommand) {
        final String name = newCommand.getClass().getSimpleName();
        if (name.compareTo(className) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isChanging() {
        return true;
    }

    @Override
    public boolean isReverting(Command newCommand) {
        return false;
    }

    @Override
    public String toString() {
        return "[" + className + "]";
    }
}

