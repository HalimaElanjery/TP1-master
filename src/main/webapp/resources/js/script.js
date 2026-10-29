function copierTexte(idElement) {
    const element = document.getElementById(idElement);
    if (element) {
        navigator.clipboard.writeText(element.value);
    }
}