// Save scroll position before form submission
document.querySelectorAll('form').forEach(form => {
    form.addEventListener('submit', function () {
        localStorage.setItem('scrollPosition', window.scrollY);
    });
});

// Restore scroll position after page load
window.addEventListener('load', function () {
    const scrollPosition = localStorage.getItem('scrollPosition');
    if (scrollPosition) {
        window.scrollTo(0, parseInt(scrollPosition)); // Scroll to the saved position
        localStorage.removeItem('scrollPosition'); // Remove it after use
    }
});

//profits and losses windows scrypt

$(document).ready(function () {
    // Load current month budget lines
    $.ajax({
        url: '/currentMonthBudgets',
        type: 'GET',
        success: function (data) {
            var profitsList = $('#profits-list');
            var expensesList = $('#losses-list');

            // Clear existing list items
            profitsList.empty();
            expensesList.empty();

            // Loop through the data and append items to the appropriate list
            data.forEach(function (budget) {
                // Format the date (assuming date is in ISO format like "2023-10-21")
                var dateAdded = new Date(budget.date).toLocaleDateString();

                if (budget.type === 'profit') {
                    profitsList.append('<li>' + budget.description + ' - $' + budget.amount + ' - ' + dateAdded + '</li>');
                } else if (budget.type === 'loss') {
                    expensesList.append('<li>' + budget.description + ' - $' + budget.amount + ' - ' + dateAdded + '</li>');
                }
            });
        },
        error: function (error) {
            console.log("Error fetching budget lines:", error);
        }
    });
});


//Adding new category

function toggleNewCategoryInput(selectElement, formType) {
    var newCategoryGroup = document.getElementById('new-category-group-' + formType);
    var newCategoryInput = document.getElementById('new-category-' + formType);

    if (selectElement.value === 'Other') {
        newCategoryGroup.style.display = 'flex'; // Show the new category input field
    } else {
        newCategoryGroup.style.display = 'none'; // Hide the new category input field
        newCategoryInput.value = ''; // Clear the input when not needed
    }
}

// Handle form submission for both add-profit and add-expense forms
document.getElementById('add-profit').addEventListener('submit', function(e) {
    handleCategorySubmission('profit');
});

document.getElementById('add-loss').addEventListener('submit', function(e) {
    handleCategorySubmission('loss');
});

function handleCategorySubmission(formType) {
    var categorySelect = document.getElementById(formType + '-category');
    var newCategoryInput = document.getElementById('new-category-' + formType);

    // If 'Other' is selected and the new category input is filled
    if (categorySelect.value === 'Other' && newCategoryInput.value.trim() !== '') {
        var newCategoryValue = newCategoryInput.value.trim().replace(/\s+/g, '_'); // Replace spaces with underscores

        // Create a new option element with the user's input
        var newOption = document.createElement('option');
        newOption.value = newCategoryValue;
        newOption.text = newCategoryValue;

        // Add the new option to the select dropdown
        categorySelect.add(newOption);

        // Set the new option as the selected value
        categorySelect.value = newCategoryValue;
    }
}






