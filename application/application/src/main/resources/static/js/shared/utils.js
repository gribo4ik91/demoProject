window.EcoTrackerUtils = (() => {
    function escapeHtml(value) {
        return String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function parseNullableNumber(value) {
        const trimmed = value.trim();
        return trimmed ? Number(trimmed) : null;
    }

    function parseNullableInteger(value) {
        const trimmed = value.trim();
        return trimmed ? Number.parseInt(trimmed, 10) : null;
    }

    function resolveApiErrorMessage(error, fallbackMessage) {
        if (!error) {
            return fallbackMessage;
        }

        if (error.validationErrors) {
            const firstValidationMessage = Object.values(error.validationErrors)[0];
            if (firstValidationMessage) {
                return String(firstValidationMessage);
            }
        }

        return error.message || fallbackMessage;
    }

    function parseEncodedPayload(value) {
        return JSON.parse(decodeURIComponent(value));
    }

    function valueToText(value) {
        return value === null || value === undefined ? '' : String(value);
    }

    return {
        escapeHtml,
        parseNullableNumber,
        parseNullableInteger,
        resolveApiErrorMessage,
        parseEncodedPayload,
        valueToText
    };
})();
