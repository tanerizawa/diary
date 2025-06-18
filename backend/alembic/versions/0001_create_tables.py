from alembic import op
import sqlalchemy as sa

revision = '0001'
down_revision = None
branch_labels = None
depends_on = None

def upgrade():
    op.create_table(
        'users',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('email', sa.String(), nullable=False, unique=True, index=True),
        sa.Column('hashed_password', sa.String(), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=True, server_default=sa.text('1')),
    )
    op.create_table(
        'journalentries',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('title', sa.String(), index=True),
        sa.Column('content', sa.Text()),
        sa.Column('mood', sa.String()),
        sa.Column('timestamp', sa.BigInteger(), nullable=False, index=True),
        sa.Column('owner_id', sa.Integer(), sa.ForeignKey('users.id')),
        sa.Column('sentiment_score', sa.Float(), nullable=True),
        sa.Column('key_emotions', sa.String(), nullable=True),
    )
    op.create_table(
        'chatmessages',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('text', sa.Text()),
        sa.Column('is_user', sa.Boolean(), server_default=sa.text('1')),
        sa.Column('timestamp', sa.BigInteger(), nullable=False, index=True),
        sa.Column('owner_id', sa.Integer(), sa.ForeignKey('users.id')),
        sa.Column('sentiment_score', sa.Float(), nullable=True),
        sa.Column('key_emotions', sa.Text(), nullable=True),
    )


def downgrade():
    op.drop_table('chatmessages')
    op.drop_table('journalentries')
    op.drop_table('users')
