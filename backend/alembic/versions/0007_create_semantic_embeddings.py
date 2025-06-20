from alembic import op
import sqlalchemy as sa

revision = '0007'
down_revision = '0006'
branch_labels = None
depends_on = None


def upgrade():
    op.create_table(
        'semantic_embeddings',
        sa.Column('id', sa.Integer(), primary_key=True),
        sa.Column('owner_id', sa.Integer(), sa.ForeignKey('users.id'), index=True),
        sa.Column('source_type', sa.String(), nullable=False),
        sa.Column('source_id', sa.Integer(), nullable=False),
        sa.Column('embedding', sa.JSON(), nullable=False),
    )


def downgrade():
    op.drop_table('semantic_embeddings')
