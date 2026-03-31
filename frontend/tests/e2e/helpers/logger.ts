import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const repoRoot = path.resolve(__dirname, '../../../../')
const logFile = path.join(repoRoot, 'logs', 'e2e-tests.log')

export function log(testName: string, step: string, message: string): void {
  const timestamp = new Date().toISOString()
  const entry = `[${timestamp}] [${testName}] [${step}] ${message}\n`
  fs.mkdirSync(path.dirname(logFile), { recursive: true })
  fs.appendFileSync(logFile, entry)
}
