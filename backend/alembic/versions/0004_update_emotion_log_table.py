from alembic import op
import sqlalchemy as sa

revision = '0004'
down_revision = '0003'
branch_labels = None
depends_on = None


def upgrade():
    op.rename_table('emotionlogs', 'emotion_logs')
    op.add_column('emotion_logs', sa.Column('sentiment_score', sa.Float(), nullable=True))
    op.add_column('emotion_logs', sa.Column('key_emotions_detected', sa.JSON(), nullable=True))


def downgrade():
    op.drop_column('emotion_logs', 'key_emotions_detected')
    op.drop_column('emotion_logs', 'sentiment_score')
    op.rename_table('emotion_logs', 'emotionlogs')
