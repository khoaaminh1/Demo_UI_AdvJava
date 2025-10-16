/**
 * Personal Finance Tracker - JavaScript Functions
 * Chart rendering and interactive features
 */

// Chart.js default configuration
Chart.defaults.font.family = "'Inter', sans-serif";
Chart.defaults.color = '#64748b';
Chart.defaults.borderColor = '#e2e8f0';

/**
 * Render a doughnut chart for category spending
 */
function renderDoughnut(canvasId, labels, data) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) {
    console.error('Canvas element not found:', canvasId);
    return;
  }

  // Validate data
  if (!labels || !data || labels.length === 0 || data.length === 0) {
    console.warn('No data available for doughnut chart');
    ctx.parentElement.innerHTML += '<p class="text-center text-muted" style="padding: 2rem;">No spending data available for this month</p>';
    return;
  }

  // Color palette
  const colors = [
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', 
    '#f59e0b', '#10b981', '#3b82f6', '#06b6d4',
    '#14b8a6', '#84cc16'
  ];

  try {
    new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: colors,
          borderWidth: 0,
          hoverOffset: 10
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              },
              usePointStyle: true,
              pointStyle: 'circle'
            }
          },
          tooltip: {
            backgroundColor: '#1e293b',
            padding: 12,
            titleFont: {
              size: 14,
              weight: '600'
            },
            bodyFont: {
              size: 13
            },
            borderColor: '#334155',
            borderWidth: 1,
            displayColors: true,
            callbacks: {
              label: function(context) {
                let label = context.label || '';
                if (label) {
                  label += ': ';
                }
                label += '$' + context.parsed.toLocaleString('en-US', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2
                });
                
                // Calculate percentage
                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                const percentage = ((context.parsed / total) * 100).toFixed(1);
                label += ` (${percentage}%)`;
                
                return label;
              }
            }
          }
        },
        cutout: '65%'
      }
    });
  } catch (error) {
    console.error('Error rendering doughnut chart:', error);
  }
}

/**
 * Render a line chart for cash flow
 */
function renderLine(canvasId, labels, incomeData, expenseData) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) {
    console.error('Canvas element not found:', canvasId);
    return;
  }

  // Validate data
  if (!labels || !incomeData || !expenseData || labels.length === 0) {
    console.warn('No data available for line chart');
    ctx.parentElement.innerHTML += '<p class="text-center text-muted" style="padding: 2rem;">No cash flow data available</p>';
    return;
  }

  try {
    new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Income',
            data: incomeData,
            borderColor: '#10b981',
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointHoverRadius: 6,
            pointBackgroundColor: '#10b981',
            pointBorderColor: '#fff',
            pointBorderWidth: 2
          },
          {
            label: 'Expense',
            data: expenseData,
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointHoverRadius: 6,
            pointBackgroundColor: '#ef4444',
            pointBorderColor: '#fff',
            pointBorderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              },
              usePointStyle: true,
              pointStyle: 'circle'
            }
          },
          tooltip: {
            backgroundColor: '#1e293b',
            padding: 12,
            titleFont: {
              size: 14,
              weight: '600'
            },
            bodyFont: {
              size: 13
            },
            borderColor: '#334155',
            borderWidth: 1,
            displayColors: true,
            callbacks: {
              label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                  label += ': $';
                }
                label += context.parsed.y.toLocaleString('en-US', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2
                });
                return label;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return '$' + value.toLocaleString('en-US');
              }
            },
            grid: {
              color: '#f1f5f9'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  } catch (error) {
    console.error('Error rendering line chart:', error);
  }
}

/**
 * Render a bar chart
 */
function renderBar(canvasId, labels, data, label = 'Amount') {
  const ctx = document.getElementById(canvasId);
  if (!ctx) return;

  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: label,
        data: data,
        backgroundColor: '#6366f1',
        borderRadius: 6,
        borderSkipped: false
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: '#1e293b',
          padding: 12,
          titleFont: {
            size: 14,
            weight: '600'
          },
          bodyFont: {
            size: 13
          },
          callbacks: {
            label: function(context) {
              return '$' + context.parsed.y.toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
              });
            }
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value) {
              return '$' + value.toLocaleString('en-US');
            }
          },
          grid: {
            color: '#f1f5f9'
          }
        },
        x: {
          grid: {
            display: false
          }
        }
      }
    }
  });
}

/**
 * Format currency
 */
function formatCurrency(amount) {
  return '$' + parseFloat(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

/**
 * Show notification (can be extended with a toast library)
 */
function showNotification(message, type = 'info') {
  console.log(`[${type.toUpperCase()}] ${message}`);
  // TODO: Implement toast notification
}

/**
 * Confirm delete action
 */
function confirmDelete(message = 'Are you sure you want to delete this item?') {
  return confirm(message);
}

/**
 * Initialize tooltips (if using a library)
 */
function initTooltips() {
  // TODO: Initialize tooltips if needed
}

/**
 * Format date to readable format
 */
function formatDate(dateString) {
  const date = new Date(dateString);
  const options = { year: 'numeric', month: 'short', day: 'numeric' };
  return date.toLocaleDateString('en-US', options);
}

/**
 * Update budget progress bar color based on percentage
 */
function updateBudgetProgress() {
  const progressBars = document.querySelectorAll('.progress .bar');
  progressBars.forEach(bar => {
    const width = parseFloat(bar.style.width);
    if (width >= 90) {
      bar.classList.add('danger');
    } else if (width >= 75) {
      bar.classList.add('warning');
    }
  });
}

// Initialize on page load - but don't interfere with chart rendering
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeApp);
} else {
  // DOM already loaded
  initializeApp();
}

function initializeApp() {
  console.log('App initialized');
  updateBudgetProgress();
  
  // Add smooth scroll behavior
  document.documentElement.style.scrollBehavior = 'smooth';
  
  // Form validation feedback
  const forms = document.querySelectorAll('.form');
  forms.forEach(form => {
    form.addEventListener('submit', function(e) {
      const inputs = form.querySelectorAll('input[required], select[required]');
      let isValid = true;
      
      inputs.forEach(input => {
        if (!input.value) {
          isValid = false;
          input.style.borderColor = '#ef4444';
        } else {
          input.style.borderColor = '#e2e8f0';
        }
      });
      
      if (!isValid) {
        e.preventDefault();
        showNotification('Please fill in all required fields', 'error');
      }
    });
  });
}
