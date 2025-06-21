from alembic import op
import sqlalchemy as sa

revision = '0008'
down_revision = '0007'
branch_labels = None
depends_on = None


def upgrade():
    op.add_column('journalentries', sa.Column('primary_emotion', sa.String(), nullable=True))
    op.add_column('chatmessages', sa.Column('primary_emotion', sa.String(), nullable=True))
    op.add_column('emotion_logs', sa.Column('primary_emotion', sa.String(), nullable=True))


def downgrade():
    op.drop_column('emotion_logs', 'primary_emotion')
    op.drop_column('chatmessages', 'primary_emotion')
    op.drop_column('journalentries', 'primary_emotion')
