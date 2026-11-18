import { test, expect } from '@playwright/test';

test('app shell loads successfully', async ({ page }) => {
  await page.goto('/');
  await expect(page.locator('h1')).toHaveText('Worker Attendance PWA');
  await expect(page.getByText('System Shell Ready')).toBeVisible();
});

