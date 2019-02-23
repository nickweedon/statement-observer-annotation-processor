package com.redwyvern.statementobserver.codemodel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
public class CodeLine implements Comparable, Serializable {

    private static final long serialVersionUID = -4239621729150324598L;

    private final ClassMethodCode classMethodCode;
    private final String code;
    private final int index;
    private final int lineNumber;
    private Statement statement;




    @Override
    public int compareTo(Object o) {

        if(!(o instanceof CodeLine)) {
            throw new RuntimeException("CodeLine is not comparable to class of type '" + o.getClass() + "'");
        }
        CodeLine rhs = (CodeLine) o;
        return index - rhs.index;
    }
}
