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
 * Command is an interface that all Commands are based.
 */
package phillockett65.PDFBookGen.Command;


public interface Command {

    /**
     * Execute the contained command.
     */
    public void execute();

    /**
     * Undo the contained command.
     */
    public void undo();

    /**
     * Redo the contained command
     */
    public void redo();

    /**
     * Attempt to use newCommand to update this command. 
     * Both must be the same command.
     * @param newCommand with the latest setting.
     * @return true if an update was performed, false otherwise.
     */
    public boolean update(Command newCommand);

    /**
     * Check if this command causes a change, i.e. the old and new values are 
     * different.
     * @return true if this command causes a change, false otherwise.
     */
    public boolean isChanging();

    /**
     * Check if newCommand undoes what this command is doing, i.e. the old 
     * value of one is the new value of the other and vice versa. 
     * Both must be the same command.
     * @param newCommand with the latest setting.
     * @return true if an update was performed, false otherwise.
     */
    public boolean isReverting(Command newCommand);

    /**
     * Construct a string representation of the command.
     * @return the string representation of the command.
     */
    public String toString();

}

