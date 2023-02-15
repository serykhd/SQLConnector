package ru.serykhd.mysql;

import lombok.Getter;

/**
 * @author Unidentified Person
 */
@Getter
public final class BufferedExecutionWithGeneratedKeys extends BufferedExecution {

    private final int generatedKey;

    public BufferedExecutionWithGeneratedKeys(int generatedKey, int affectedRows) {
        super(affectedRows);
        this.generatedKey = generatedKey;
    }
}
