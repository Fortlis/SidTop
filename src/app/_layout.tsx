import "@/../global.css";
import Error from "@/components/ui/Fallbacks/Error";
import LoadingComponent from "@/components/ui/Fallbacks/Loading";
import { db } from "@/db/client";
import migrations from "@/db/migrations/migrations";
import { useOnboardingStatus } from "@/hooks/useOnboardingStatus";
import * as AlarmSyncService from "@/services/alarm-sync.service";
import { useMigrations } from "drizzle-orm/expo-sqlite/migrator";
import { Stack } from "expo-router";
import { useEffect } from "react";

function DatabaseProvider({ children }: { children: React.ReactNode }) {
  const { success, error } = useMigrations(db, migrations);

  if (error) {
    console.error("Migration error:", error);
    return <Error />;
  }

  if (!success) {
    return <LoadingComponent />;
  }

  return <>{children}</>;
}

export default function RootLayout() {
  const onboarded = useOnboardingStatus()

  useEffect(() => {
    AlarmSyncService.start()
    return () => AlarmSyncService.stop()
  }, [])

  return (
    <DatabaseProvider>
      <Stack screenOptions={{headerShown: false}}>
        <Stack.Protected guard={!onboarded}>
          <Stack.Screen name='onboarding' />
        </Stack.Protected>

        <Stack.Protected guard={onboarded}>
          <Stack.Screen name='(tabs)' />
        </Stack.Protected>
      </Stack>
    </DatabaseProvider>
  );
}