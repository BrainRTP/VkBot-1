/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package jolyjdia.bot.newcalculator.internal.expression.runtime;

import jolyjdia.bot.newcalculator.internal.expression.Expression;
import jolyjdia.bot.newcalculator.internal.expression.parser.ParserException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Function extends Node {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Dynamic { }

    final Method method;
    final RValue[] args;

    Function(int position, Method method, RValue... args) {
        super(position);
        this.method = method;
        this.args = args;
    }

    @Override
    public final double getValue() throws EvaluationException {
        return invokeMethod(method, args);
    }

    static double invokeMethod(@NotNull Method method, Object[] args) throws EvaluationException {
        try {
            return (Double) method.invoke(null, args);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof EvaluationException) {
                throw (EvaluationException) e.getTargetException();
            }
            throw new EvaluationException(-1, "Exception caught while evaluating expression", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new EvaluationException(-1, "Internal error while evaluating expression", e);
        }
    }

    @Override
    public final String toString() {
        final StringBuilder ret = new StringBuilder(method.getName()).append('(');
        boolean first = false;
        for (Object obj : args) {
            if (first) {
                ret.append(", ");
            }
            first = true;
            ret.append(obj);
        }
        return ret.append(')').toString();
    }

    @Override
    public char id() {
        return 'f';
    }

    @Override
    public RValue optimize() throws EvaluationException {
        final RValue[] optimizedArgs = new RValue[args.length];
        boolean optimizable = !method.isAnnotationPresent(Dynamic.class);
        int position = getPosition();
        for (int i = 0; i < args.length; ++i) {
            final RValue optimized = optimizedArgs[i] = args[i].optimize();

            if (!(optimized instanceof Constant)) {
                optimizable = false;
            }

            if (optimized.getPosition() < position) {
                position = optimized.getPosition();
            }
        }

        return optimizable ?
                new Constant(invokeMethod(method, optimizedArgs)) :
                new Function(position, method, optimizedArgs);
    }

    @Override
    public RValue bindVariables(Expression expression) throws ParserException {
        for (int i = 0; i < args.length; ++i) {
            args[i] = args[i].bindVariables(expression);
        }

        return this;
    }

}
