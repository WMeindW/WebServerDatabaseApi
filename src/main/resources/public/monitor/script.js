/**
 * Fetches data from a JSON file and processes it to generate statistics.
 *
 * @returns {Promise<Array>} A promise that resolves to an array of objects containing statistics for each unique path.
 */
document.addEventListener("DOMContentLoaded", () => {
    async function fetchData() {
        try {
            const response = await fetch("data.json", {
                method: "GET"
            });
            let data = await response.text();
            return JSON.parse("[" + data.substring(0, data.length - 2) + "]");
        } catch (error) {
            console.error("Error fetching events:", error);
            return [];
        }
    }

    fetchData().then((data) => {
        const container = document.getElementById("container");
        const paths = [];
        for (const d of data) {
            if (!paths.includes(d.path)) paths.push(d.path);
        }
        const dataObjects = [];
        for (const path of paths) {
            const d = data.filter(data => data.path === path);
            let totalTime = 0;
            let averageTime = 0;
            d.forEach(d => totalTime += d.servingTime)
            if (d.length !== 0) averageTime = totalTime / d.length;
            dataObjects.push({
                path: path,
                errors: d.filter(d => d.error === true).length,
                averageTime: averageTime
            });
        }
        for (const dataObject of dataObjects) {
            const div = document.createElement("div");
            div.innerHTML = `
                <h2>${dataObject.path}</h2>
                <p>Errors: ${dataObject.errors}</p>
                <p>Average serving time: ${Math.round( dataObject.averageTime * 10) / 10} miliseconds</p>
            `;
            container.appendChild(div);
        }
        console.log(dataObjects);
    });
});