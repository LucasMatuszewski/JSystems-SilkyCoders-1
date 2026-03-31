/**
 * Backend health check utilities for E2E tests
 */

const BACKEND_URL = 'http://localhost:8080'
const HEALTH_ENDPOINT = `${BACKEND_URL}/actuator/health`
const TIMEOUT_MS = 5000

/**
 * Verify that the backend is running and healthy
 * @throws Error if backend is not responding or health check fails
 */
export async function verifyBackendRunning(): Promise<void> {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS)

  try {
    const response = await fetch(HEALTH_ENDPOINT, {
      signal: controller.signal,
    })

    if (!response.ok) {
      throw new Error(
        `Backend health check failed: ${response.status} ${response.statusText}`
      )
    }

    const data = await response.json()
    if (data.status !== 'UP') {
      throw new Error(`Backend is not healthy: ${JSON.stringify(data)}`)
    }
  } catch (error) {
    if (error instanceof Error) {
      if (error.name === 'AbortError') {
        throw new Error(
          `Backend health check timed out after ${TIMEOUT_MS}ms. ` +
            `Make sure the backend is running at ${BACKEND_URL}`
        )
      }
      throw new Error(
        `Backend is not running or not accessible: ${error.message}\n` +
          `Start the backend with: cd backend && ./mvnw spring-boot:run`
      )
    }
    throw error
  } finally {
    clearTimeout(timeoutId)
  }
}

/**
 * Wait for backend to be healthy with retries
 * @param maxRetries - Maximum number of retries (default: 30)
 * @param retryIntervalMs - Milliseconds between retries (default: 1000)
 */
export async function waitForBackend(
  maxRetries: number = 30,
  retryIntervalMs: number = 1000
): Promise<void> {
  for (let i = 0; i < maxRetries; i++) {
    try {
      await verifyBackendRunning()
      return
    } catch (error) {
      if (i === maxRetries - 1) {
        throw error
      }
      // Wait before retrying
      await new Promise((resolve) => setTimeout(resolve, retryIntervalMs))
    }
  }
}
