import SessionList from "@/components/ui/SessionList/SessionList";
import { db } from "@/db/client";
import { sessions as sessionsTable } from "@/db/schema";
import { desc } from "drizzle-orm";
import { useLiveQuery } from "drizzle-orm/expo-sqlite";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HistoryScreen() {
    const { data: sessions } = useLiveQuery(
        db.select().from(sessionsTable).orderBy(desc(sessionsTable.triggerAtMillis))
    )

    return (
        <SafeAreaView className="flex-1 mt-1">
            <SessionList sessions={sessions} state={true} cancelReason={true}/>
        </SafeAreaView>
    )
}
