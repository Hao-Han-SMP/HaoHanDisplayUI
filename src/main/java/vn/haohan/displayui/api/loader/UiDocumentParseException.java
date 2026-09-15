/*
 * Copyright (C) 2026 HaoHanSMP
 *
 * This file is part of HaoHanDisplayUI.
 *
 * HaoHanDisplayUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HaoHanDisplayUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HaoHanDisplayUI. If not, see <https://www.gnu.org/licenses/>.
 */
package vn.haohan.displayui.api.loader;

/**
 * Exception thrown when parsing a UI template or layout document file (typically {@code .hhdui.json}) fails.
 * <p>
 * Occurs when JSON syntax is malformed, required schema nodes are invalid, or attribute values fail validation.
 */
public final class UiDocumentParseException extends RuntimeException {

    /**
     * Constructs a parse exception with a detailed error message.
     *
     * @param message descriptive error message detailing the parse failure
     */
    public UiDocumentParseException(String message) {
        super(message);
    }

    /**
     * Constructs a parse exception with a message and underlying cause.
     *
     * @param message descriptive error message
     * @param cause   underlying cause (e.g., IOException, JsonSyntaxException)
     */
    public UiDocumentParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

