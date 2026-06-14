import type { AdminUserDetailsResponse } from '../../../../types/dto';
import UserStatCard from './UserStatCard';

interface SelectedUserStatisticsTabProps {
  userDetails: AdminUserDetailsResponse;
}

const SelectedUserStatisticsTab = ({ userDetails }: SelectedUserStatisticsTabProps) => {
  return (
    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      <UserStatCard label="Chats" value={userDetails.chatCount} />
      <UserStatCard label="Messages" value={userDetails.messageCount} />
      <UserStatCard
        label="Avg. Messages / Chat"
        value={userDetails.averageMessagesPerChat.toFixed(2)}
      />
      <UserStatCard label="CVs" value={userDetails.cvCount} />
    </div>
  );
};

export default SelectedUserStatisticsTab;
