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
 * Invoker is a class that is responsible for invoking calls and handling the 
 * do undo/redo mechanism.
 * It is implemented as a basic (non thread safe) Singleton.
 */
package phillockett65.PDFBookGen.Command;

import java.util.LinkedList;

import phillockett65.Debug.Debug;

public class Invoker {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private static Invoker instance = new Invoker();
    private LinkedList<Command> undoStack = new LinkedList<>();
    private LinkedList<Command> redoStack = new LinkedList<>();
    private LinkedList<Command> doQueue = new LinkedList<>();

    private Command newCommand;

    /**
     * Private default constructor - part of the Singleton Design Pattern.
     * Called at initialization only, constructs the single private instance.
     */
    private Invoker() {
    }

    /**
     * Check if 'new' command does not change the original value.
     * @return true if command changes nothing, false otherwise.
     */
    private boolean isNewCommandUnchanging() {
        return !newCommand.isChanging();
    }

    /**
     * Check if 'new' command is triggered by a handler after an undo.
     * @return true if 'new' command is triggered by handler, false otherwise. 
     */
    private boolean isHandlerCausingARevert() {
        if (redoStack.isEmpty()) {
            return false;
        }

        return redoStack.peek().isReverting(newCommand);
    }

    /**
     * Check if a 'current' command is unavailable.
     * @return true if there is no 'current' command, false otherwise.
     */
    private boolean isNoCurrentCommand() {
        return undoStack.isEmpty();
    }

    /**
     * Attempt to update the 'current' command with the 'new' command.
     * Assumes there is a 'current' command available.
     * @return true if the update was successful, false otherwise.
     */
    private boolean isACurrentCommandUpdate() {
        return undoStack.peek().update(newCommand);
    }

    /**
     * Check if the 'current' command no longer changes it's original value.
     * Assumes there is a 'current' command available.
     * @return true if the 'current' command is unchanging, false otherwise.
     */
    private boolean isCurrentCommandUnchanging() {
        return !undoStack.peek().isChanging();
    }

    /**
     * Execute the 'new' command.
     */
    private void executeNewCommand() {
        newCommand.execute();
    }

    /**
     * Stack the 'new' command in case there is an undo request and clear out 
     * the redo stack.
     */
    private void pushNewCommand() {
        undoStack.push(newCommand);

        redoStack.clear();
    }



    /************************************************************************
     * Support code for public interface.
     */

    /**
     * Singleton implementation.
     * @return the only instance of the model.
     */
    public static Invoker getInstance() { return instance; }

    /**
     * Execute the given new command and push it to the undo stack making it 
     * the current command.
     * @param command to execute.
     */
    public void invoke(Command command) {
        newCommand = command;
        Debug.trace(DD, "invoke(" + newCommand + ")");

        // If 'new' command does not change it's value, silently drop it.
        if (isNewCommandUnchanging()) {
            Debug.info(DD, "Dropped unchanged");

            return;
        }

        // If 'new' command is a handler revert, silently drop it.
        if (isHandlerCausingARevert()) {
            Debug.info(DD, "Dropped revert");

            return;
        }

        // No 'current' command, so just excute and stack it.
        if (isNoCurrentCommand()) {
            executeNewCommand();
            pushNewCommand();

            Debug.info(DD, "Pushed first ");

            return;
        }

        // If this is an update to the 'current' command, execute it.
        if (isACurrentCommandUpdate()) {
            executeNewCommand();

            final Command current = undoStack.peek();
            Debug.info(DD, "Updated and " + (current.isChanging() ? "different" : "SAME") + " ");

            return;
        }

        // If 'current' command has been changed back to original value, 
        // silently drop it before we handle the 'new' command.
        if (isCurrentCommandUnchanging()) {
            Command dropped = undoStack.pop();
            Debug.info(DD, "Dropping unchange current command ");
            Debug.info(DD, dropped.toString());
        }

        // Handle the new command.
        executeNewCommand();
        pushNewCommand();

        Debug.info(DD, "Pushed ");
    }

    /**
     * Undo the top command on the undo stack and move it to the redo stack.
     */
    public void undo() {
        Debug.trace(DD, "undo received " + undoStack.size());
        if (undoStack.isEmpty()) {
            return;
        }

        Command command = undoStack.pop();
        redoStack.push(command);
        command.undo();
    }

    /**
     * Redo the top command on the redo stack and move it to the undo stack.
     */
    public void redo() {
        Debug.trace(DD, "redo received " + redoStack.size());
        if (redoStack.isEmpty()) {
            return;
        }

        Command command = redoStack.pop();
        undoStack.push(command);
        command.redo();
    }

    /**
     * Queue the command ready to be executed later.
     * @param command to execute later.
     */
    public void queue(Command command) {
        doQueue.push(command);
    }

    /**
     * Execute all queued commands.
     */
    public void executeQueue() {
        for (Command c = doQueue.pollLast(); c != null; c = doQueue.pollLast()) {
            c.execute();
        }
    }

    /**
     * Clear all stacks and queues.
     */
    public void clear() {
        Debug.trace(DD, "clear received");
        undoStack.clear();
        redoStack.clear();
        doQueue.clear();
    }

    /**
     * Debug.
     */
    public void dump() {
        Debug.info(DD, "undoStack: " + undoStack);
        Debug.info(DD, "redoStack: " + redoStack);
        Debug.info(DD, "doQueue: " + doQueue);
        Debug.info(DD, "");
    }

}

