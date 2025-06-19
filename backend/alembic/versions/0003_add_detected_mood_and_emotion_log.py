from alembic import op
import sqlalchemy as sa

revision = '0003'
down_revision = '0002'
branch_labels = None
depends_on = None


def upgrade():
    op.add_column('chatmessages', sa.Column('detected_mood', sa.String(), nullable=True))
    op.create_table(
        'emotionlogs',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('user_id', sa.Integer(), sa.ForeignKey('users.id')),
        sa.Column('timestamp', sa.BigInteger(), nullable=False, index=True),
        sa.Column('detected_mood', sa.String(), nullable=True),
        sa.Column('source_text', sa.Text()),
        sa.Column('source_feature', sa.String()),
    )


def downgrade():
    op.drop_table('emotionlogs')
    op.drop_column('chatmessages', 'detected_mood')
