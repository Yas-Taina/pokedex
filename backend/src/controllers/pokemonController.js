const pool = require('../config/database');

exports.registerPokemon = async (req, res) => {
  const { pokemon_id, pokemon_name, types, ability, moves, user_login } = req.body;

  if (!pokemon_id || !pokemon_name || !types || !ability || !moves || !user_login) {
    return res.status(400).json({ error: 'Todos os campos são obrigatórios' });
  }

  try {
    const checkDuplicate = await pool.query(
      'SELECT * FROM registered_pokemons WHERE pokemon_name = $1',
      [pokemon_name]
    );

    if (checkDuplicate.rows.length > 0) {
      return res.status(409).json({
          error: `O Pokémon ${pokemon_name} já foi registrado por outro treinador.`
      });
    }

    const result = await pool.query(
      `INSERT INTO registered_pokemons
       (pokemon_id, pokemon_name, types, ability, moves, user_login)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING *`,
      [pokemon_id, pokemon_name, types, ability, moves, user_login]
    );

    res.status(201).json({
      message: 'Pokémon registrado com sucesso',
      pokemon: result.rows[0]
    });
  } catch (error) {
    console.error('Erro ao registrar pokémon:', error);
    res.status(500).json({ error: 'Erro ao registrar pokémon' });
  }
};

exports.getUserPokemons = async (req, res) => {
  const { user_login } = req.params;

  try {
    const result = await pool.query(
      'SELECT * FROM registered_pokemons WHERE user_login = $1 ORDER BY pokemon_id',
      [user_login]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao buscar pokémons:', error);
    res.status(500).json({ error: 'Erro ao buscar pokémons' });
  }
};

exports.checkRegisteredPokemons = async (req, res) => {
  try {
    const result = await pool.query('SELECT pokemon_id FROM registered_pokemons');

    const registeredIds = result.rows.map(row => row.pokemon_id);
    res.json(registeredIds);
  } catch (error) {
    console.error('Erro ao verificar pokémons:', error);
    res.status(500).json({ error: 'Erro ao verificar pokémons' });
  }
};

exports.updatePokemon = async (req, res) => {
  const { id } = req.params;
  const { ability, moves } = req.body;

  if (!ability || !moves) {
    return res.status(400).json({ error: 'Habilidade e movimentos são obrigatórios' });
  }

  try {
    const result = await pool.query(
      'UPDATE registered_pokemons SET ability = $1, moves = $2 WHERE id = $3 RETURNING *',
      [ability, moves, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json({
      message: 'Pokémon atualizado com sucesso',
      pokemon: result.rows[0]
    });
  } catch (error) {
    console.error('Erro ao atualizar pokémon:', error);
    res.status(500).json({ error: 'Erro ao atualizar pokémon' });
  }
};

exports.deletePokemon = async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(
      'DELETE FROM registered_pokemons WHERE id = $1 RETURNING *',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json({ message: 'Pokémon deletado com sucesso' });
  } catch (error) {
    console.error('Erro ao deletar pokémon:', error);
    res.status(500).json({ error: 'Erro ao deletar pokémon' });
  }
};

exports.getPokemonDetails = async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(
      'SELECT * FROM registered_pokemons WHERE id = $1',
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Pokémon não encontrado' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Erro ao buscar detalhes:', error);
    res.status(500).json({ error: 'Erro ao buscar detalhes' });
  }
};

exports.getHomeStats = async (req, res) => {
  try {
    const totalResult = await pool.query(
      'SELECT COUNT(*) as total FROM registered_pokemons'
    );

    const typesResult = await pool.query(
      `SELECT unnest(string_to_array(types, ',')) as type, COUNT(*) as count
       FROM registered_pokemons
       GROUP BY type
       ORDER BY count DESC
       LIMIT 3`
    );

    const abilitiesResult = await pool.query(
      `SELECT unnest(string_to_array(ability, ',')) as ability, COUNT(*) as count
       FROM registered_pokemons
       GROUP BY ability
       ORDER BY count DESC
       LIMIT 3`
    );

    res.json({
      total: parseInt(totalResult.rows[0].total),
      topTypes: typesResult.rows,
      topAbilities: abilitiesResult.rows
    });
  } catch (error) {
    console.error('Erro ao buscar estatísticas:', error);
    res.status(500).json({ error: 'Erro ao buscar estatísticas' });
  }
};

exports.searchByType = async (req, res) => {
  const { type } = req.params;

  try {
    const result = await pool.query(
      `SELECT * FROM registered_pokemons
       WHERE types LIKE $1
       ORDER BY pokemon_id`,
      [`%${type}%`]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao pesquisar por tipo:', error);
    res.status(500).json({ error: 'Erro ao pesquisar por tipo' });
  }
};

exports.searchByAbility = async (req, res) => {
  const { ability } = req.params;

  try {
    const result = await pool.query(
      `SELECT * FROM registered_pokemons
       WHERE ability ILIKE $1
       ORDER BY pokemon_id`,
      [`%${ability}%`]
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Erro ao pesquisar por habilidade:', error);
    res.status(500).json({ error: 'Erro ao pesquisar por habilidade' });
  }
};

exports.getAllPokemons = async (req, res) => {
    try {
        const query = 'SELECT * FROM registered_pokemons ORDER BY id ASC';
        const { rows } = await pool.query(query);

        res.status(200).json(rows);
    } catch (error) {
        console.error('Erro ao buscar todos os pokémons:', error);
        res.status(500).json({ error: 'Erro ao buscar pokémons' });
    }
};